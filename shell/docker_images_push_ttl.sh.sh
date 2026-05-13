# 生成一个随机 UUID (Linux/macOS)
# 如果是 Windows Power Shell，可以使用 [guid]::NewGuid().ToString()
export MY_UUID=$(uuidgen | tr '[:upper:]' '[:lower:]')

# 构建并打上 ttl.sh 标签（有效期设为 2h）
# 镜像名格式：ttl.sh/项目名-UUID:有效期
docker build -t "ttl.sh/modbus-vertx-${MY_UUID}:2h" ..

# 推送镜像
docker push "ttl.sh/modbus-vertx-${MY_UUID}:2h"

# 打印出最终的镜像地址，保存好这个地址，下一步要用
echo "Your image is: ttl.sh/modbus-vertx-${MY_UUID}:2h"
