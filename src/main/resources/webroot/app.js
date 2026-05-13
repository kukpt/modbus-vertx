const pageSize = 10;

const state = {
    activeTab: "locators",
    locators: {page: 1, totalPages: 1, rows: []},
    templates: {page: 1, totalPages: 1, rows: []},
    devices: {page: 1, totalPages: 1, rows: []}
};

const tabMeta = {
    locators: {
        title: "寄存器定位",
        hint: "维护 RegisterLocator，并供模板批量关联。"
    },
    templates: {
        title: "寄存器模板",
        hint: "维护 RegisterTemplate，支持批量关联 RegisterLocator。"
    },
    devices: {
        title: "Modbus 设备",
        hint: "维护 ModbusDevice，并可调用应用配置接口刷新连接。"
    }
};

const locatorTypes = ["BINARY_LOCATOR", "STRING_LOCATOR", "NUMERIC_LOCATOR"];
const rangeNames = {
    1: "COIL_STATUS",
    2: "INPUT_STATUS",
    3: "HOLDING_REGISTER",
    4: "INPUT_REGISTER"
};
const rangeDataTypeNames = {
    1: "BINARY",
    2: "TWO_BYTE_INT_UNSIGNED",
    3: "TWO_BYTE_INT_SIGNED",
    22: "TWO_BYTE_INT_UNSIGNED_SWAPPED",
    23: "TWO_BYTE_INT_SIGNED_SWAPPED",
    4: "FOUR_BYTE_INT_UNSIGNED",
    5: "FOUR_BYTE_INT_SIGNED",
    6: "FOUR_BYTE_INT_UNSIGNED_SWAPPED",
    7: "FOUR_BYTE_INT_SIGNED_SWAPPED",
    24: "FOUR_BYTE_INT_UNSIGNED_SWAPPED_SWAPPED",
    25: "FOUR_BYTE_INT_SIGNED_SWAPPED_SWAPPED",
    8: "FOUR_BYTE_FLOAT",
    9: "FOUR_BYTE_FLOAT_SWAPPED",
    21: "FOUR_BYTE_FLOAT_SWAPPED_INVERTED",
    10: "EIGHT_BYTE_INT_UNSIGNED",
    11: "EIGHT_BYTE_INT_SIGNED",
    12: "EIGHT_BYTE_INT_UNSIGNED_SWAPPED",
    13: "EIGHT_BYTE_INT_SIGNED_SWAPPED",
    14: "EIGHT_BYTE_FLOAT",
    15: "EIGHT_BYTE_FLOAT_SWAPPED",
    16: "TWO_BYTE_BCD",
    17: "FOUR_BYTE_BCD",
    20: "FOUR_BYTE_BCD_SWAPPED",
    18: "CHAR",
    19: "VARCHAR",
    26: "FOUR_BYTE_MOD_10K",
    27: "SIX_BYTE_MOD_10K",
    28: "EIGHT_BYTE_MOD_10K",
    29: "FOUR_BYTE_MOD_10K_SWAPPED",
    30: "SIX_BYTE_MOD_10K_SWAPPED",
    31: "EIGHT_BYTE_MOD_10K_SWAPPED",
    32: "ONE_BYTE_INT_UNSIGNED_LOWER",
    33: "ONE_BYTE_INT_UNSIGNED_UPPER"
};
const $ = (id) => document.getElementById(id);

let entityModal;

function setStatus(message, isError = false) {
    const target = $("statusLine");
    target.textContent = message;
    target.style.color = isError ? "#b42318" : "#687586";
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: {"Content-Type": "application/json"},
        ...options
    });
    const text = await response.text();
    const body = text ? JSON.parse(text) : null;
    if (!response.ok || (body && body.code !== 0)) {
        throw new Error((body && body.msg) || `请求失败：${response.status}`);
    }
    return body ? body.data : null;
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#039;");
}

function valueOrDash(value) {
    return value === null || value === undefined || value === "" ? "-" : escapeHtml(value);
}

function toNumber(value) {
    return value === "" || value === null || value === undefined ? null : Number(value);
}

function getRegisterTemplateId(device) {
    return device.registerTemplateId || (device.registerTemplate && device.registerTemplate.id) || null;
}

async function loadSelectRows(endpoint) {
    const result = await api(`${endpoint}/1/1000`);
    return result.data || [];
}

function locatorSummary(locator) {
    if (!locator) {
        return "-";
    }
    const range = rangeNames[locator.registerRange] || locator.registerRange || "-";
    return `${locator.name || "未命名"} · ${range} · ${locator.registerOffset ?? "-"}`;
}

function locatorBadges(locators) {
    if (!locators || locators.length === 0) {
        return '<span class="badge warn">未关联</span>';
    }
    return locators.map((item) => `<span class="badge">${escapeHtml(locatorSummary(item))}</span>`).join("");
}

function templateName(template) {
    return template ? `${template.name || "未命名"} #${template.id}` : "未选择";
}

async function loadLocators(page = state.locators.page) {
    const result = await api(`/device/locator/list/${page}/${pageSize}`);
    state.locators = {
        page: result.page,
        totalPages: result.totalPages || 1,
        rows: result.data || []
    };
    renderLocators();
}

async function loadTemplates(page = state.templates.page) {
    const result = await api(`/device/template/list/${page}/${pageSize}`);
    state.templates = {
        page: result.page,
        totalPages: result.totalPages || 1,
        rows: result.data || []
    };
    renderTemplates();
}

async function loadDevices(page = state.devices.page) {
    const result = await api(`/device/list/${page}/${pageSize}`);
    state.devices = {
        page: result.page,
        totalPages: result.totalPages || 1,
        rows: result.data || []
    };
    renderDevices();
}

function renderLocators(rows = state.locators.rows) {
    const tbody = $("locatorsTable");
    if (!rows.length) {
        tbody.innerHTML = '<tr><td colspan="10" class="empty">暂无寄存器定位</td></tr>';
    } else {
        tbody.innerHTML = rows.map((row) => `
      <tr>
        <td>${row.id}</td>
        <td>${valueOrDash(row.name)}</td>
        <td>${valueOrDash(row.tagName)}</td>
        <td>${valueOrDash(row.type)}</td>
        <td>${valueOrDash(row.slaveId)}</td>
        <td>${valueOrDash(rangeNames[row.registerRange] || row.registerRange)}</td>
        <td>${valueOrDash(row.registerOffset)}</td>
        <td>${valueOrDash(rangeDataTypeNames[row.dataType] || row.dataType)}</td>
        <td>${valueOrDash(row.registerBit)}</td>
        <td>
          <div class="row-actions">
            <button class="text" data-action="edit-locator" data-id="${row.id}">编辑</button>
            <button class="text danger" data-action="delete-locator" data-id="${row.id}">删除</button>
          </div>
        </td>
      </tr>
    `).join("");
    }
    renderPager("locators");
}

function renderTemplates(rows = state.templates.rows) {
    const tbody = $("templatesTable");
    if (!rows.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty">暂无寄存器模板</td></tr>';
    } else {
        tbody.innerHTML = rows.map((row) => `
      <tr>
        <td>${row.id}</td>
        <td>${valueOrDash(row.name)}</td>
        <td>${valueOrDash(row.version)}</td>
        <td>${locatorBadges(row.registerLocators)}</td>
        <td>
          <div class="row-actions">
            <button class="text" data-action="edit-template" data-id="${row.id}">编辑</button>
            <button class="text danger" data-action="delete-template" data-id="${row.id}">删除</button>
          </div>
        </td>
      </tr>
    `).join("");
    }
    renderPager("templates");
}

function renderDevices(rows = state.devices.rows) {
    const tbody = $("devicesTable");
    if (!rows.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty">暂无 Modbus 设备</td></tr>';
    } else {
        tbody.innerHTML = rows.map((row) => `
      <tr>
        <td>${row.id}</td>
        <td>${valueOrDash(row.name)}</td>
        <td>${valueOrDash(row.useIp)}:${valueOrDash(row.usePort)}</td>
        <td>${valueOrDash(row.onlineState)}</td>
        <td>${row.getOnlyChanged ? "是" : "否"}</td>
        <td>${valueOrDash(templateName(row.registerTemplate))}</td>
        <td>
          <div class="row-actions">
            <button class="text" data-action="apply-device" data-id="${row.id}">应用</button>
            <button class="text" data-action="edit-device" data-id="${row.id}">编辑</button>
            <button class="text danger" data-action="delete-device" data-id="${row.id}">删除</button>
          </div>
        </td>
      </tr>
    `).join("");
    }
    renderPager("devices");
}

function renderPager(kind) {
    const data = state[kind];
    const target = $(`${kind}Pager`);
    target.innerHTML = `
    <span>第 ${data.page} / ${data.totalPages || 1} 页</span>
    <button data-action="page-${kind}" data-page="${Math.max(1, data.page - 1)}" ${data.page <= 1 ? "disabled" : ""}>上一页</button>
    <button data-action="page-${kind}" data-page="${Math.min(data.totalPages || 1, data.page + 1)}" ${data.page >= (data.totalPages || 1) ? "disabled" : ""}>下一页</button>
  `;
}

function switchTab(tab) {
    state.activeTab = tab;
    document.querySelectorAll(".nav-tab").forEach((button) => {
        button.classList.toggle("active", button.dataset.tab === tab);
    });
    document.querySelectorAll(".panel").forEach((panel) => panel.classList.remove("active"));
    $(`${tab}Panel`).classList.add("active");
    $("pageTitle").textContent = tabMeta[tab].title;
    $("pageHint").textContent = tabMeta[tab].hint;
}

function openDialog(title, html, onSubmit) {
    $("dialogTitle").textContent = title;
    $("entityForm").innerHTML = html;
    $("entityForm").onsubmit = async (event) => {
        event.preventDefault();
        try {
            await onSubmit(new FormData(event.currentTarget), event.currentTarget);
            closeDialog();
        } catch (error) {
            setStatus(error.message, true);
        }
    };
    if (entityModal) {
        entityModal.show();
    }
}

function closeDialog() {
    if (entityModal) {
        entityModal.hide();
    }
    $("entityForm").onsubmit = null;
}

function formShell(fields) {
    return `<div class="form-grid">${fields}</div>
    <div class="form-actions">
      <button type="button" data-action="close-dialog">取消</button>
      <button class="primary" type="submit">保存</button>
    </div>`;
}

function inputField(name, label, value = "", type = "text", extra = "") {
    return `<div class="field">
    <label for="${name}">${label}</label>
    <input id="${name}" name="${name}" type="${type}" value="${escapeHtml(value ?? "")}" ${extra}>
  </div>`;
}

function selectField(name, label, value, options, allowBlank = true) {
    const blank = allowBlank ? '<option value="">请选择</option>' : "";
    return `<div class="field">
    <label for="${name}">${label}</label>
    <select id="${name}" name="${name}">
      ${blank}
      ${options.map((option) => {
        const optionValue = typeof option === "object" ? option.value : option;
        const optionLabel = typeof option === "object" ? option.label : option;
        return `<option value="${escapeHtml(optionValue)}" ${String(optionValue) === String(value ?? "") ? "selected" : ""}>${escapeHtml(optionLabel)}</option>`;
    }).join("")}
    </select>
  </div>`;
}

function checkboxField(name, label, checked) {
    return `<div class="field">
    <label>${label}</label>
    <label class="check-row"><input name="${name}" type="checkbox" ${checked ? "checked" : ""}> 是</label>
  </div>`;
}

function locatorForm(locator = {}) {
    return formShell(`
    ${inputField("name", "名称", locator.name, "text", "required")}
    ${inputField("tagName", "标签名", locator.tagName)}
    ${selectField("type", "定位器类型", locator.type, locatorTypes)}
    ${inputField("slaveId", "从站地址", locator.slaveId, "number", "required")}
    ${selectField("registerRange", "寄存器范围", locator.registerRange, [
        {value: 1, label: "1 - COIL_STATUS"},
        {value: 2, label: "2 - INPUT_STATUS"},
        {value: 3, label: "3 - HOLDING_REGISTER"},
        {value: 4, label: "4 - INPUT_REGISTER"}
    ], false)}
    ${inputField("registerOffset", "地址偏移", locator.registerOffset, "number", "required")}
    ${selectField("dataType", "数据类型", locator.dataType, [
        {value: 1, label: "1 - BINARY"},
        {value: 2, label: "2 - TWO_BYTE_INT_UNSIGNED"},
        {value: 3, label: "3 - TWO_BYTE_INT_SIGNED"},
        {value: 22, label: "22 - TWO_BYTE_INT_UNSIGNED_SWAPPED"},
        {value: 23, label: "23 - TWO_BYTE_INT_SIGNED_SWAPPED"},
        {value: 4, label: "4 - FOUR_BYTE_INT_UNSIGNED"},
        {value: 5, label: "5 - FOUR_BYTE_INT_SIGNED"},
        {value: 6, label: "6 - FOUR_BYTE_INT_UNSIGNED_SWAPPED"},
        {value: 7, label: "7 - FOUR_BYTE_INT_SIGNED_SWAPPED"},
        {value: 24, label: "24 - FOUR_BYTE_INT_UNSIGNED_SWAPPED_SWAPPED"},
        {value: 25, label: "25 - FOUR_BYTE_INT_SIGNED_SWAPPED_SWAPPED"},
        {value: 8, label: "8 - FOUR_BYTE_FLOAT"},
        {value: 9, label: "9 - FOUR_BYTE_FLOAT_SWAPPED"},
        {value: 21, label: "21 - FOUR_BYTE_FLOAT_SWAPPED_INVERTED"},
        {value: 10, label: "10 - EIGHT_BYTE_INT_UNSIGNED"},
        {value: 11, label: "11 - EIGHT_BYTE_INT_SIGNED"},
        {value: 12, label: "12 - EIGHT_BYTE_INT_UNSIGNED_SWAPPED"},
        {value: 13, label: "13 - EIGHT_BYTE_INT_SIGNED_SWAPPED"},
        {value: 14, label: "14 - EIGHT_BYTE_FLOAT"},
        {value: 15, label: "15 - EIGHT_BYTE_FLOAT_SWAPPED"},
        {value: 16, label: "16 - TWO_BYTE_BCD"},
        {value: 17, label: "17 - FOUR_BYTE_BCD"},
        {value: 20, label: "20 - FOUR_BYTE_BCD_SWAPPED"},
        {value: 18, label: "18 - CHAR"},
        {value: 19, label: "19 - VARCHAR"},
        {value: 26, label: "26 - FOUR_BYTE_MOD_10K"},
        {value: 27, label: "27 - SIX_BYTE_MOD_10K"},
        {value: 28, label: "28 - EIGHT_BYTE_MOD_10K"},
        {value: 29, label: "29 - FOUR_BYTE_MOD_10K_SWAPPED"},
        {value: 30, label: "30 - SIX_BYTE_MOD_10K_SWAPPED"},
        {value: 31, label: "31 - EIGHT_BYTE_MOD_10K_SWAPPED"},
        {value: 32, label: "32 - ONE_BYTE_INT_UNSIGNED_LOWER"},
        {value: 33, label: "33 - ONE_BYTE_INT_UNSIGNED_UPPER"}
    ], false)}
    ${inputField("registerBit", "寄存器位", locator.registerBit ?? -1, "number")}
  `);
}

function locatorPayload(formData, id) {
    return {
        ...(id ? {id} : {}),
        name: formData.get("name"),
        tagName: formData.get("tagName") || null,
        type: formData.get("type") || null,
        slaveId: toNumber(formData.get("slaveId")),
        registerRange: toNumber(formData.get("registerRange")),
        registerOffset: toNumber(formData.get("registerOffset")),
        dataType: toNumber(formData.get("dataType")),
        registerBit: toNumber(formData.get("registerBit"))
    };
}

async function showLocatorDialog(locator = null) {
    const id = locator && locator.id;
    openDialog(id ? `编辑定位 #${id}` : "新增寄存器定位", locatorForm(locator || {}), async (formData) => {
        await api("/device/locator", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(locatorPayload(formData, id))
        });
        await loadLocators();
        setStatus(id ? "定位器已更新" : "定位器已新增");
    });
}

function locatorChoices(locators, selectedIds = []) {
    const selected = new Set(selectedIds.map(String));
    if (!locators.length) {
        return '<div class="empty">暂无可选定位器，请先新增 RegisterLocator。</div>';
    }
    return `<div class="choice-grid">
    ${locators.map((locator) => `
      <label class="choice">
        <input type="checkbox" name="locators" value="${locator.id}" ${selected.has(String(locator.id)) ? "checked" : ""}>
        <span>${escapeHtml(locatorSummary(locator))}</span>
      </label>
    `).join("")}
  </div>`;
}

function templateForm(template = {}, locators = []) {
    const selectedIds = (template.registerLocators || []).map((item) => item.id);
    return formShell(`
    ${inputField("name", "名称", template.name, "text", "required")}
    ${inputField("version", "版本", template.version, "number", "required")}
    <div class="field full">
      <label>批量关联定位器</label>
      ${locatorChoices(locators, selectedIds)}
    </div>
  `);
}

function templatePayload(formData, id) {
    const locatorIds = formData.getAll("locators").map(Number);
    if (id) {
        return {
            id,
            name: formData.get("name"),
            version: toNumber(formData.get("version")),
            locators: locatorIds
        };
    }
    return {
        name: formData.get("name"),
        version: toNumber(formData.get("version")),
        registerLocators: locatorIds.map((locatorId) => ({id: locatorId}))
    };
}

async function showTemplateDialog(template = null) {
    const locators = await loadSelectRows("/device/locator/list");
    const id = template && template.id;
    openDialog(id ? `编辑模板 #${id}` : "新增寄存器模板", templateForm(template || {}, locators), async (formData) => {
        await api("/device/template", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(templatePayload(formData, id))
        });
        await loadTemplates();
        setStatus(id ? "模板已更新" : "模板已新增");
    });
}

function templateOptions(templates) {
    return templates.map((template) => ({
        value: template.id,
        label: `${template.name || "未命名"} #${template.id}`
    }));
}

function deviceForm(device = {}, templates = []) {
    return formShell(`
    ${inputField("name", "名称", device.name, "text", "required")}
    ${selectField("registerTemplateId", "寄存器模板", getRegisterTemplateId(device), templateOptions(templates), false)}
    ${inputField("useIp", "IP 地址", device.useIp, "text", "required")}
    ${inputField("usePort", "端口", device.usePort, "number", "required")}
    ${selectField("onlineState", "在线状态", device.onlineState, ["ONLINE", "OFFLINE"])}
    ${checkboxField("getOnlyChanged", "仅变化上报", device.getOnlyChanged)}
  `);
}

function devicePayload(formData, id) {
    return {
        ...(id ? {id} : {}),
        name: formData.get("name"),
        useIp: formData.get("useIp"),
        usePort: toNumber(formData.get("usePort")),
        onlineState: formData.get("onlineState") || null,
        getOnlyChanged: formData.get("getOnlyChanged") === "on",
        registerTemplateId: toNumber(formData.get("registerTemplateId"))
    };
}

async function showDeviceDialog(device = null) {
    const templates = await loadSelectRows("/device/template/list");
    const id = device && device.id;
    openDialog(id ? `编辑设备 #${id}` : "新增 Modbus 设备", deviceForm(device || {}, templates), async (formData) => {
        await api("/device/", {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(devicePayload(formData, id))
        });
        await loadDevices();
        setStatus(id ? "设备已更新" : "设备已新增");
    });
}

async function findById(kind, inputId, endpoint, render) {
    const id = $(inputId).value;
    if (!id) {
        throw new Error("请输入 ID");
    }
    const item = await api(`${endpoint}/${id}`);
    render([item]);
    setStatus(`已查询到 ${kind} #${id}`);
}

async function handleAction(action, target) {
    if (!action) {
        return;
    }
    if (action === "close-dialog") {
        closeDialog();
        return;
    }

    const id = Number(target.dataset.id);
    try {
        if (action === "refresh-locators") await loadLocators(1);
        if (action === "refresh-templates") await loadTemplates(1);
        if (action === "refresh-devices") await loadDevices(1);
        if (action === "new-locator") await showLocatorDialog();
        if (action === "new-template") await showTemplateDialog();
        if (action === "new-device") await showDeviceDialog();
        if (action === "edit-locator") await showLocatorDialog(await api(`/device/locator/${id}`));
        if (action === "edit-template") await showTemplateDialog(await api(`/device/template/${id}`));
        if (action === "edit-device") await showDeviceDialog(await api(`/device/${id}`));
        if (action === "delete-locator" && confirm(`确认删除定位器 #${id}？`)) {
            await api(`/device/locator/${id}`, {method: "DELETE"});
            await loadLocators();
            setStatus("定位器已删除");
        }
        if (action === "delete-template" && confirm(`确认删除模板 #${id}？`)) {
            await api(`/device/template/${id}`, {method: "DELETE"});
            await loadTemplates();
            setStatus("模板已删除");
        }
        if (action === "delete-device" && confirm(`确认删除设备 #${id}？`)) {
            await api(`/device/${id}`, {method: "DELETE"});
            await loadDevices();
            setStatus("设备已删除");
        }
        if (action === "apply-device") {
            await api(`/device/apply/${id}`, {method: "POST"});
            setStatus(`设备 #${id} 配置已应用`);
        }
        if (action === "find-locator") await findById("定位器", "locatorQueryId", "/device/locator", renderLocators);
        if (action === "find-template") await findById("模板", "templateQueryId", "/device/template", renderTemplates);
        if (action === "find-device") await findById("设备", "deviceQueryId", "/device", renderDevices);
        if (action.startsWith("page-")) {
            const kind = action.replace("page-", "");
            const page = Number(target.dataset.page);
            if (kind === "locators") await loadLocators(page);
            if (kind === "templates") await loadTemplates(page);
            if (kind === "devices") await loadDevices(page);
        }
    } catch (error) {
        setStatus(error.message, true);
    }
}

document.addEventListener("click", (event) => {
    const tab = event.target.closest(".nav-tab");
    if (tab) {
        switchTab(tab.dataset.tab);
        return;
    }
    const actionTarget = event.target.closest("[data-action]");
    if (actionTarget) {
        handleAction(actionTarget.dataset.action, actionTarget);
    }
});

document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") {
        closeDialog();
    }
});

async function init() {
    const modalElement = $("entityModal");
    entityModal = window.bootstrap && modalElement ? new bootstrap.Modal(modalElement) : null;
    if (modalElement) {
        modalElement.addEventListener("hidden.bs.modal", () => {
            $("entityForm").onsubmit = null;
        });
    }
    try {
        await Promise.all([loadLocators(1), loadTemplates(1), loadDevices(1)]);
        setStatus("数据已加载");
    } catch (error) {
        setStatus(error.message, true);
    }
}

init();
