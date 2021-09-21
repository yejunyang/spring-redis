
local customreMapKey = KEYS[1]
local customerOrderKey = KEYS[2]
local userOrderKey = KEYS[3]
local customerId = ARGV[1]
local userKeyPre = ARGV[2]
local strategy = ARGV[3]
local customer = {}
local user = {}
local allMatchUserId = {}

local function split(str,reps)
    local resultStrList = {}
    string.gsub(str,'[^'..reps..']+',function (w)
        table.insert(resultStrList,w)
    end)
    return resultStrList
end

local customerDetail = redis.call('HGETALL', customreMapKey)
if customerDetail == nil then
    return 'offLine3'
end
for i=1,#customerDetail,2 do
    customer[customerDetail[i]] = customerDetail[i+1]
end
if customer.userId ~= nil and customer.userId ~= '' then
    return customer.userId
end
local customerSet = redis.call('ZRANGE', customerOrderKey, 0, -1)
if customerSet == nil or #customerSet == 0 then
    return 'offLine1'
end
local customerInOrder = false
for k, v in pairs(customerSet) do
    if v == customerId then
        customerInOrder = true
        break
    end
end
if not customerInOrder then
    return 'offLine2'
end
local userSet = redis.call('ZRANGE', userOrderKey, 0, -1)
if userSet == nil or #userSet == 0 then
    return 'noUser'
end
local num = 1;
for k,v in pairs(userSet) do
    local userMap = redis.call('HGETALL', userKeyPre .. v)
    local userDetail = {}
    if userMap ~= nil then
        for i=1,#userMap,2 do
            userDetail[userMap[i]] = userMap[i+1]
        end
        if userDetail.customerId == nil or userDetail.customerId == '' then
            user[num] = userDetail
            num = num + 1
        end
    end
end
if user == nil or #user == 0 then
    return 'noUser'
end
local customerParam = cjson.decode(customer.params)
for k,v in pairs(user) do
    local score = 0;
    local userParams = cjson.decode(v.params)
    if userParams == nil then
        return "customerParamError"
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
        table.insert(allMatchUserId,1,v.id)
    end
end

if #allMatchUserId == 0 then
    return 'noMatchUser'
end

local matchUserId = allMatchUserId[math.random(#allMatchUserId)]
redis.call('HSET',userKeyPre..matchUserId,'customerId',customerId)
redis.call('HSET',customreMapKey,'userId',matchUserId)
redis.call('zrem',customerOrderKey,customerId)

return matchUserId
