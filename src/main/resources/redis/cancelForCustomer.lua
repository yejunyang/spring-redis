local customreMapKey = KEYS[1]
local customerOrderKey = KEYS[2]
local customerId = ARGV[1]
local result = {}
local customer = {}

local customerMapString = redis.call('HGET', customreMapKey,customerId)
local customerSet = redis.call('ZRANGE', customerOrderKey, 0, -1)

local isInOrder = false
if customerSet ~= nil and #customerSet > 0 then
    for k, v in pairs(customerSet) do
        if v == customerId then
            isInOrder = true
            break
        end
    end
end

if isInOrder then
    local customerMap = redis.call('HGETALL', customreMapKey)
    if customerMap == nil then
        result.type = 'ALREADY_OFFLINE'
        return cjson.encode(result)
    end
    if customerMap ~= nil then
        for i=1,#customerMap,2 do
            if customerMap[i] == customerId then
                customer = cjson.decode(customerMap[i+1])
                break
            end
        end
    end
    redis.call('zrem',customerOrderKey,customerId)
    redis.call('HDEL',customreMapKey,customerId)
    result.type = 'SUCCESSFUL'
    if customer ~= nil then
        result.data = customer
    end
    return cjson.encode(result)
else
    if customerMapString == nil or not customerMapString then
        result.type = 'ALREADY_OFFLINE'
        return cjson.encode(result)
    else
        result.type = 'DOING'
        return cjson.encode(result)
    end
end

