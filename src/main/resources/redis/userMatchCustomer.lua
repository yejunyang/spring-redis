
local customreMapKey = KEYS[1]
local customreOrderKey = KEYS[2]
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

local customerSet = redis.call('ZRANGE', customreOrderKey, 0, -1)
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
local customerDetail = redis.call('HGETALL', customreMapKey)
if customerDetail == nil then
    return 'offLine3'
end
for i=1,#customerDetail,2 do
    customer[customerDetail[i]] = customerDetail[i+1]
end
local num = 1;
for k,v in pairs(userSet) do
    local userMap = redis.call('HGETALL', userKeyPre .. v)
    local userDetail = {}
    if userMap ~= nil then
        for i=1,#userMap,2 do
            userDetail[userMap[i]] = userMap[i+1]
        end
        if userMap.customerId == nil then
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
        return "num"
    end

    for ck, cv in pairs(customerParam) do
        for uk, uv in pairs(userParams) do
            if cv.key == uv.key then
                if cv.type == 'EQUAL' and cv.value == uv.value then
                    score = score + 1
                elseif cv.type == 'CONTAIN' then
                    local userParamList = split(uv.value, ",")
                    for k,v in pairs(userParamList) do
                        if v == cv.value then
                            score = score + 1
                        end
                    end
                elseif cv.type == 'COMPARE' then
                    local userParamList = split(uv.value, ",")
                    local min = userParamList[1]
                    local max = userParamList[1]
                    if #userParamList == 2 then
                        max = userParamList[2]
                    end
                    if cv.value >= min then
                        score = score + 1
                    end
                    if cv.value <= max then
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
redis.call('zrem',customreOrderKey,customerId)

return matchUserId
