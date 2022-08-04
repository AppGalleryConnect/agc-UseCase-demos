const agconnect = require('@agconnect/common-server');
const clouddb = require('@agconnect/database-server/dist/index.js');
const BookInfo = require('./model/BookInfo.js');
const SubscriptionRecord = require('./model/SubscriptionRecord.js');

let api_client_name = "./model/agc-apiclient-test.json";
let path = require('path');
let api_client_path = path.join(__dirname, api_client_name);
agconnect.AGCClient.initialize(agconnect.CredentialParser.toCredential(api_client_path));

let cloudDBZoneClient;
let resp = -11;
let isSubscription = false;


var http = require('http'); //引入https模块
var url = require('url'); //引入url模块
var querystring = require('querystring'); // 引入querystring模块

module.exports.myHandler = async function (event, context, callback, logger) {

    try {
        logger.info("event start");
        这种方式引入
        logger.info(JSON.stringify(event));
        logger.info("event start " + event.body.phone);

        await initDB(callback, logger);

        let body = JSON.parse(event.body);//接收http触发器请求参数
        if (body.type == "1") {//插入订阅用户
            const record = getSingleSubscriptionRecord(body.articleId, body.authorId, body.phone, body.uid, logger);
            await upsertSubscriptionRecord(record, callback, logger);

        } else if (body.type == "2") {//查询订阅用户
            await querySubscriptionRecordWithOrder(body.authorId, body.phone, callback, logger);

        } else {//test
            // const bookInfo = getSingleBook(logger);
            // await upsertBookInfos(bookInfo, callback, logger);
        }
 

        let result = { "message": "ok", "isSubscription": isSubscription, "body": body, "type": body.type }
        logger.info(result)

        callback(result);

    } catch (error) {
        let result = { "main message": error.message }
        logger.info(result)
        callback(result);
    }
}

//初始化云数据库
async function initDB(callback, logger) {
    logger.info('start ----------- initDB');
    try {
        const agcClient = agconnect.AGCClient.getInstance();
        clouddb.AGConnectCloudDB.initialize(agcClient);
        const agconnectCloudDB = clouddb.AGConnectCloudDB.getInstance(agcClient);
        const cloudDBZoneConfig = new clouddb.CloudDBZoneConfig('ArticleZone');
        cloudDBZoneClient = agconnectCloudDB.openCloudDBZone(cloudDBZoneConfig);

        let result = { "message": "openCloudDBZone success" };
        logger.info(result);
    } catch (error) {
        let result = { "initDB message": error.message };
        logger.info(result);
        callback(result);
    }
}

//SubscriptionRecord    插入用户订阅信息到云数据库
async function upsertSubscriptionRecord(SubscriptionRecord, callback, logger) {
    logger.info('start ----------- upsertSubscriptionRecord');

    if (!cloudDBZoneClient) {
        console.log("CloudDBClient is null, try re-initialize it");
        let result = { "message": "CloudDBClient is null" };
        logger.info(result);
        // callback(result);
        return;
    }
    try {
        resp = await cloudDBZoneClient.executeUpsert(SubscriptionRecord);
        console.log('The number of upsert record is:', resp);
        let result = { "message": "upsertSubscriptionRecord success " };
        logger.info(result);
    } catch (error) {
        let result = { "upsert err ": error.message };
        logger.info(result);
        callback(result);
    }
}

//查询订阅列表，用户是否订阅
async function querySubscriptionRecordWithOrder(authorId, phone, callback, logger) {
    if (!cloudDBZoneClient) {
        console.log("CloudDBClient is null, try re-initialize it");
        return;
    }
    try {
        const cloudDBZoneQuery = clouddb.CloudDBZoneQuery.where(SubscriptionRecord.SubscriptionRecord).equalTo("authorId", parseInt(authorId)).equalTo("phone", phone);
        const resp = await cloudDBZoneClient.executeQuery(cloudDBZoneQuery);
        console.log('The number of query records is:', resp.getSnapshotObjects().length);
        isSubscription = resp.getSnapshotObjects().length > 0 ? true : false;

        if (isSubscription) {
            // await sendSMS(phone);
        }

    } catch (error) {
        let result = { "query err ": error.message };
        logger.info(result);
        callback(result);
    }
}

//获取订阅记录实体
function getSingleSubscriptionRecord(id, authorId, phone, uid, logger) {
    logger.info('start ----------- getSingleSubscriptionRecord');
    let record = new SubscriptionRecord.SubscriptionRecord();
    record.setId(parseInt(id));
    record.setAuthorId(parseInt(authorId));
    record.setPhone(phone);
    record.setUid(uid);
    return record;
}

/**
 * 构造请求Body体
 * 当发送短信使用的是无变量模板时，不需要添加templateParas参数。
 * @returns
 */
function buildRequestBody() {
    var mtSmsMessage = {
        'mobiles': msisdn,
        'templateId': smsTemplateId,
        'templateParas': templateParas,
        'signature': signature
    };

    var requestLists = [mtSmsMessage];

    var requestbody = {
        'account': account,
        'password': password1,
        'requestLists': requestLists,
        'statusCallback': statusCallBack
    };

    return JSON.stringify(requestbody);
}

//发送短信提醒
async function sendSMS(phone) {
    //必填,请替换为实际值
    var realUrl = 'https://ip:port/common/sms/sendTemplateMessage'; //APP接入地址+接口访问URI
    var msisdn = ['8613900000013'];
    var smsTemplateId = 'SMS_21060200001'; //模板ID
    var templateParas = { 'code': 'hello' }; //模板变量，此处以单变量验证码短信为例，请客户自行生成6位验证码，并定义为字符串类型，以杜绝首位0丢失的问题（例如：002569变成了2569）。当发送短信使用的是无变量的模板时，请删除本行代码。
    var account = 'account';
    var password1 = 'password1';
    var signature = '【huawei】';  // 签名名称
    // 选填,短信状态报告接收地址,推荐使用域名,为空或者不填表示不接收状态报告
    var statusCallBack = 'https://ip:port/common/sms/notifyReportMessage';

    var urlobj = url.parse(realUrl); //解析realUrl字符串并返回一个 URL对象

    var options = {
        host: urlobj.hostname, //主机名
        port: urlobj.port, //端口
        path: urlobj.pathname, //URI
        method: 'POST', //请求方法为POST
        headers: { //请求Headers
            'Content-Type': 'application/json;charset=UTF-8'
        },
        rejectUnauthorized: false //为防止因HTTPS证书认证失败造成API调用失败,需要先忽略证书信任问题
    };
    // 请求Body,不携带签名名称时,signature请填null
    var body = buildRequestBody();

    var req = http.request(options, (res) => {
        console.log('statusCode:', res.statusCode); //打印响应码

        res.setEncoding('utf8'); //设置响应数据编码格式
        res.on('data', (d) => {
            console.log('resp:', d); //打印响应数据
        });
    });
    req.on('error', (e) => {
        console.error('错误：' + e.message); //请求错误时,打印错误信息
    });

    req.write(body, "utf-8"); //发送请求Body数据
    req.end(); // 结束请求

}

//test
async function upsertBookInfos(BookInfo, callback, logger) {
    logger.info('start ----------- upsertBookInfos');

    if (!cloudDBZoneClient) {
        console.log("CloudDBClient is null, try re-initialize it");
        let result = { "message": "CloudDBClient is null" };
        logger.info(result);
        // callback(result);
        return;
    }
    try {
        resp = await cloudDBZoneClient.executeUpsert(BookInfo);
        console.log('The number of upsert books is:', resp);
        let result = { "message": "upsertBookInfos success" };
        logger.info(result);
    } catch (error) {
        console.warn('upsertBo  okInfo=>', error);
        let result = { "upsert message": err.message };
        logger.info(result);
        callback(result);
    }
}


function getSingleBook(logger) {
    logger.info('start ----------- getSingleBook');
    let bookInfo = new BookInfo.BookInfo();
    bookInfo.setId(3);
    bookInfo.setBookName("Les Fleurs du mal");
    bookInfo.setAuthor("Charles Pierre Baudelaire");
    bookInfo.setPublisher("Auguste Poulet-Malassis");
    bookInfo.setPublishTime(new Date('1857-01-01T03:24:00'));
    bookInfo.setPrice(30.99);
    return bookInfo;
}

// myHandler()

// module.exports.myHandler = myHandler;