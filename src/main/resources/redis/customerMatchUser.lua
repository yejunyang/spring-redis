
local customreMapKey = KEYS[1]
local customerOrderKey = KEYS[2]
local userKey = KEYS[3]
local customerId = ARGV[1]
local customer = {}
local user = {}
local allMatchUser = {}
local result = {}

local function split(str,reps)
    local resultStrList = {}
    string.gsub(str,'[^'..reps..']+',function (w)
        table.insert(resultStrList,w)
    end)
    return resultStrList
end

local customerString = redis.call('HGET', customreMapKey,customerId)
if customerString == nil or not customerString then
    result.type = 'ALREADY_OFFLINE'
    return cjson.encode(result)
end
customer = cjson.decode(customerString)

if customer == nil then
    result.type = 'ALREADY_OFFLINE'
    return cjson.encode(result)
end

if customer.userId ~= nil and customer.userId ~= '' then
    result.type = 'SUCCESSFUL'
    local customerString = redis.call('HGET', userKey,customer.userId)
    if customerString ~= nil and customerString then
        result.data = cjson.decode(customerString)
    end
    return cjson.encode(result)
end
local customerSet = redis.call('ZRANGE', customerOrderKey, 0, -1)
if customerSet == nil or #customerSet == 0 then
    result.type = 'ALREADY_OFFLINE'
    return cjson.encode(result)
end
local customerInOrder = false
for k, v in pairs(customerSet) do
    if v == customerId then
        customerInOrder = true
        break
    end
end
if not customerInOrder then
    result.type = 'ALREADY_OFFLINE'
    return cjson.encode(result)
end
local num = 1;
local userMap = redis.call('HGETALL', userKey)
if userMap ~= nil then
    for i=1,#userMap,2 do
        local userDetail = cjson.decode(userMap[i+1])
        if (userDetail.customerId == nil or userDetail.customerId == '') and string.upper(userDetail.status) == 'IDLE' then
            user[num] = userDetail
            num = num + 1
        end
    end
end
if user == nil or #user == 0 then
    result.type = 'NO_USER'
    return cjson.encode(result)
end
local customerParam = customer.params
for k,v in pairs(user) do
    local score = 0;
    local userParams = v.params
    if userParams == nil then
        result.type = 'CUSTOMER_PARAM_ERROR'
        return cjson.encode(result)
    end

    for ck, cv in pairs(customerParam) do
        for uk,uv in pairs(userParams) do
            if cv.type == uv.type and cv.value ~= nil and uv.value ~= nil and cv.value ~= '' and uv.value ~= '' then
                if uv.type == 'EQUAL' and cv.value == uv.value then
                    score = score + 1
                elseif uv.type == 'CONTAIN' then
                    local userParamList = split(uv.value, ",")
                    for k,v in pairs(userParamList) do
                        if v == cv.value then
                            score = score + 1
                        end
                    end
                elseif uv.type == 'COMPARE' then
                    local userParamList = split(uv.value, ",")
                    local customerParamList = split(cv.value, ",")
                    local userMin = tonumber(userParamList[1])
                    local userMax = tonumber(userParamList[2])
                    local customerMin = tonumber(customerParamList[1])
                    local customerMax = customerMin
                    if #customerParamList == 2 then
                        customerMax = tonumber(customerParamList[2])
                    end
                    if customerMin >= userMin and customerMax <= userMax then
                        score = score + 1
                    end
                end
            end
        end
    end
    if score > 0 then
        table.insert(allMatchUser,1,v)
    end
end

if #allMatchUser == 0 then
    result.type = 'NO_MATCH_USER'
    return cjson.encode(result)
end

local matchUser = allMatchUser[math.random(#allMatchUser)]
matchUser.status = 'DOING'
matchUser.customerId = customerId
customer.userId = matchUser.id

redis.call('HSET',userKey,matchUser.id,cjson.encode(matchUser))
redis.call('HSET',customreMapKey,customerId,cjson.encode(customer))
redis.call('zrem',customerOrderKey,customerId)

result.type = 'SUCCESSFUL'
result.data = matchUser
return cjson.encode(result)
