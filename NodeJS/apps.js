var get_ip = require('ipware')().get_ip;
var http = require('http'),
    express = require('express'),
    app = express(),
    sqlite3 = require('sqlite3').verbose(),
    path = require('path'),
    jsonfile = require('jsonfile'),
    file = 'TempData.json',
    obj,
    db = new sqlite3.Database('NodeTemp.db');
db.get("SELECT name FROM sqlite_master WHERE type='table' AND name='TBL_Temp'",
    function(err, rows) {
        if (err !== null) {
            console.log(err);
        } else if (rows === undefined) {
            db.run("CREATE TABLE 'TBL_Temp' ('id' INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE,'value'	INTEGER NOT NULL,'time'	TEXT)",
                function(err) {
                    if (err !== null) {
                        console.log(err);
                    } else {
                        console.log("SQL Table 'TBL_Temp' initialized.");
                    }
                });
        } else {
            console.log("SQL Table 'TBL_Temp' already initialized.");
        }
    });
app.get('/', function(req, res) {
    res.sendFile(path.join(__dirname + '/index.html'));
    var ip_info = get_ip(req);
    var ip = req.headers['x-real-ip'] || req.connection.remoteAddress;
    console.log(ip);
});
app.get('/api/GetTemp', function(req, res) {
    //http://temp.haydut.xyz/api/GetTemp?GetTempToken=xxx
    var GetTempToken = req.param('GetTempToken');
    if (GetTempToken == "xxx") {
        db.all('SELECT value,time FROM TBL_Temp ORDER BY id DESC LIMIT 60', function(err, row) {
            if (err !== null) {
                next(err);
            } else {
                console.log(JSON.stringify(row));
                obj = JSON.stringify(row);
                res.send('{"JSONTemp":' + obj + '}');
                jsonfile.writeFile(file, obj, function(err) {
                    console.error(err)
                })
            }
        });
    } else {
        res.send("error GetTempToken");
    }
});
app.get('/api/SetTemp', function(req, res) {
    //http://temp.haydut.xyz/api/SetTemp?token=gz&value=25
    var token = req.param('token');
    var value = req.param('value');
    if (token == "gz") {
        sqlRequest = "INSERT INTO 'TBL_Temp'('value', 'time') " +
            "VALUES('" + value + "',strftime('%H:%M:%S', 'now','localtime'))"
        db.run(sqlRequest, function(err) {
            if (err !== null) {
                next(err);
            } else {
                res.send("ok");
                console.log(token + ":" + value);
            }
        });
    } else {
        res.send("error");
    }
});
app.listen(1907, function() {
    console.log('Example app listening on port 1907!')
})
