
local userMapKey = KEYS[1]
local customreMapKey = KEYS[2]
local customerOrderKey = KEYS[3]
local userId = ARGV[1]
local customer = {}
local user = {}
local result = {}

local function split(str,reps)
    local resultStrList = {}
    string.gsub(str,'[^'..reps..']+',function (w)
        table.insert(resultStrList,w)
    end)
    return resultStrList
end

local userString = redis.call('HGET', userMapKey,userId)
if userString == nil or not userString then
    result.type = 'ALREADY_OFFLINE'
    return cjson.encode(result)
end

user = cjson.decode(userString)

if user == nil or user == '' then
    result.type = 'ALREADY_OFFLINE'
    return cjson.encode(result)
end
if user.customerId ~= nil and user.customerId ~= '' then
    result.type = 'SUCCESSFUL'
    result.data = cjson.decode(redis.call('HGET', customreMapKey,user.customerId))
    return cjson.encode(result)
end

local customerSet = redis.call('ZRANGE', customerOrderKey, 0, -1)
if customerSet == nil or #customerSet == 0 then
    result.type = 'NO_CUSTOMER'
    return cjson.encode(result)
end
local allCustomerMap = redis.call('HGETALL', customreMapKey)
if allCustomerMap == nil or #allCustomerMap == 0 then
    result.type = 'NO_CUSTOMER'
    return cjson.encode(result)
end

local userParam = user.params

for co,vo in pairs(customerSet) do
    local isMatched = false
    for i=1,#allCustomerMap,2 do
        if allCustomerMap[i] == vo then
            local score = 0;
            local customerDetail = cjson.decode(allCustomerMap[i+1])
            local customerParams = customerDetail.params
            if customerParams ~= nil then
                for ck, cv in pairs(customerParams) do
                    for uk,uv in pairs(userParam) do
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
            end
            if score > 0 then
                customer = customerDetail
                isMatched = true
                break
            end
        end
    end
    if isMatched then
        break
    end
end

if customer == nil then
    result.type = 'NO_MATCH_CUSTOMER'
    return cjson.encode(result)
end
customer.userId = userId
user.customerId = customer.id
user.status = 'DOING'
redis.call('HSET',userMapKey,userId,cjson.encode(user))
redis.call('HSET',customreMapKey,customer.id,cjson.encode(customer))
redis.call('zrem',customerOrderKey,customer.id)

result.type = 'SUCCESSFUL'
result.data = customer
return cjson.encode(result)
