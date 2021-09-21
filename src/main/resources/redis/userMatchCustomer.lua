
local userMapKey = KEYS[1]
local userOrderKey = KEYS[2]
local customerOrderKey = KEYS[3]
local userId = ARGV[1]
local customerMapKeyPre = ARGV[2]
local customer = {}
local user = {}
local matchCustomerId = ''

local function split(str,reps)
    local resultStrList = {}
    string.gsub(str,'[^'..reps..']+',function (w)
        table.insert(resultStrList,w)
    end)
    return resultStrList
end

local userDetail = redis.call('HGETALL', userMapKey)
if userDetail == nil then
    return 'offLine3'
end
for i=1,#userDetail,2 do
    user[userDetail[i]] = userDetail[i+1]
end
if user.customerId ~= nil and user.customerId ~= '' then
    return user.customerId
end
local userSet = redis.call('ZRANGE', userOrderKey, 0, -1)
if userSet == nil or #userSet == 0 then
    return 'offLine1'
end
local userInOrder = false
for k, v in pairs(userSet) do
    if v == userId then
        userInOrder = true
        break
    end
end
if not userInOrder then
    return 'offLine2'
end
local customerSet = redis.call('ZRANGE', customerOrderKey, 0, -1)
if customerSet == nil or #customerSet == 0 then
    return 'noCustomer'
end
local num = 1;
for k,v in pairs(customerSet) do
    local customerMap = redis.call('HGETALL', customerMapKeyPre .. v)
    local customerDetail = {}
    if customerMap ~= nil then
        for i=1,#customerMap,2 do
            customerDetail[customerMap[i]] = customerMap[i+1]
        end
        if customerDetail.userId == nil or customerDetail.userId == '' then
            customer[num] = customerDetail
            num = num + 1
        end
    end
end
if customer == nil or #customer == 0 then
    return 'noCustomer'
end
local userParam = cjson.decode(user.params)
for k,v in pairs(customer) do
    local score = 0;
    local customerParams = cjson.decode(v.params)
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
        matchCustomerId = v.id
        break
    end
end

if matchCustomerId == nil or matchCustomerId == '' then
    return 'noMatchCustomer'
end

redis.call('HSET',userMapKey,'customerId',matchCustomerId)
redis.call('HSET',customerMapKeyPre..matchCustomerId,'userId',userId)
redis.call('zrem',customerOrderKey,matchCustomerId)

return matchCustomerId
