local userKey = KEYS[1]
local targetStatus = ARGV[1]
local userId = ARGV[2]
local user = {}
local result = {}

local userDetailString = redis.call('HGET', userKey,userId)

if userDetailString == nil or userDetailString == '' or not userDetailString then
    result.type = 'ALREADY_OFFLINE'
    return cjson.encode(result)
end

user = cjson.decode(userDetailString)

if user.customerId == nil or user.customerId == '' then
    if targetStatus == 'OFFLINE' then
        redis.call('HDEL',userKey,userId)
    else
        user.status = targetStatus
        redis.call('HSET',userKey,userId,cjson.encode(user))
    end
    result.type = 'SUCCESSFUL'
else
    result.type = 'DOING'
end
if targetStatus ~= 'OFFLINE' then
    result.data = user
end

return cjson.encode(result)