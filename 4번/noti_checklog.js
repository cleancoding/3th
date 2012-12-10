var util  = require('util');
var log4js = require('log4js');
var logger = log4js.getLogger('notifier');
	logger.setLevel('DEBUG');
var CONFIG    = require('config');
var redis = require('redis');
	client = redis.createClient(CONFIG.Redis[0].port, CONFIG.Redis[0].ip);
	client.on("error", function(err) {
		logger.error("Error " + err);
	});
// For sending mail to smtp
var nodemailer = require('nodemailer');
var async = require('async');
// For sending sms to mex-mt
var http = require('http');
var node_uuid = require('node-uuid');
var _ = require('underscore');
var mysql = require('mysql');
var mysql_config = {
	host     : CONFIG.MySql.host,
	port     : CONFIG.MySql.port,
	user     : CONFIG.MySql.user,
	password : CONFIG.MySql.password,
	database : CONFIG.MySql.database, };
var current_time;

exports.send_alarm_to_email = function() {
	// Create a SMTP transport object
	var transport = nodemailer.createTransport("SMTP", {
		host: "smail01.nhncorp.com", // hostname
		port: 25
	});

	var send_result_count = 0;
	var max_send_size = 100;

	_(max_send_size).times(function() { 
		client.lpop("alert_mail", function (err, reply) {
			if ( err != null ) {
				logger.error("client.lpop: " + err );
			} else if ( reply != null ) { 
				message = eval('('+reply+')');
				transport.sendMail(message, function(error){
					if(error){
						logger.error('send mail Error occured:', error.message);
					} else {
						logger.debug('Message sent successfully!');
					}
					if ( ++send_result_count == max_send_size) {
						logger.info('Mail Sender completed (' + send_result_count + ' mail send)');
						transport.close(); // close the connection pool
						return;
					}
				});
			}
		});
	});
};

function save_alarm_email_to_redis(log, rule, uuid, alarm_interval) 
{
	var email_subject, email_text;

	logger.info(JSON.stringify(log));
	email_subject = log.project_name;
	if ( log.project_version != undefined && log.project_version.length > 0 ) {
		email_subject += ' (v' + log.project_version + ')';
	}
	if ( alarm_interval > 0 ) {
		email_subject += ' in the last ' + alarm_interval + ' minutes';
	}
	email_subject += ', ';
	email_subject += log.log_msg.substring(0,255) + ' errors occurred.';

	email_text = email_subject + '\n';
	if ( rule.alarm_each_host == 'Y' ) 
		email_text += 'Host: ' + log.host + '\n';
	email_text += 'The most recent time for the error occurred is ' + 
				 (new Date(Number(log.log_time))).toLocaleString(); 
	email_text += '\nGo to nelo2 page : http://nlog.nhncorp.com/logview/' + uuid;

	// Message object
	var message = { 
		// sender info
		from: 'NELO Admin <nelo-admin@nhn.com>',

		// Comma separated list of recipients
		to: "sungduk.yu@nhn.com",
//		to: rule.mail,

		// Subject of the message
		subject: '[NELO Alert] ' + email_subject,

		// plaintext body
		text: email_text
	};

	//logger.info('Sending Mail:' + rule.mail);
	logger.debug('[TEST] Sending Mail:sungduk.yu@nhn.com');
	logger.debug('[TEST] '+email_text);

	client.rpush("alert_mail", JSON.stringify(message), function (err, reply) {
		if ( err != null ) {
			logger.error("client.rpush: " + err );
		}
	});
}

function send_text_to_mex (path, callback) {
	var mex_dev_host  = 'alpha.mobile.nhncorp.com';
	var mex_real_host = 'bloc.mex.nhncorp.com';
	var options = {
		host: mex_dev_host,
		port: 5001,
		path: path,
		method: 'GET'
	};

	var req = http.request(options, function(res) {
		var response_body = '';
		res.setEncoding('utf8');
		res.on('data', function (chunk) {
			response_body += chunk;
		});
		res.on('end', function () {
			callback(response_body);
		});
	});

	req.on('error', function(e) {
		logger.error('send mex problem with request: ' + e.message);
	});

	req.end();
}

function send_alarm_to_sms(log, rule, alarm_interval) 
{
	var sms_text;

	sms_text = log.project_name;
	if ( log.project_version != undefined && log.project_version.length > 0 ) {
		sms_text += ' (v' + log.project_version + ')';
	}

	if ( alarm_interval > 0 ) {
		sms_text += ' in the last ' + alarm_interval + ' minutes';
	}
	sms_text += ', ';
	sms_text += log.log_msg.substring(0,255) + ' errors occurred.';
	if ( rule.alarm_each_host == 'Y' ) 
		sms_text += 'Host: ' + log.host;

	var test_phone = rule.mobile;
	mobile_list = test_phone.split(",");
	for (idx in mobile_list) {
		mobile = mobile_list[idx].split(" ").join("");
		var mex_call = util.format("/mex-mt-server/SendBO/sendSMS?serviceId=\"ESM00200\"&sendMdn=\"%s\"&receiveMdnList=[\"%s\"]&content=\"%s\"", mobile, mobile, sms_text);
		logger.debug(mex_call);

		logger.info('Sending sms ' + rule.mobile);

		send_text_to_mex( mex_call, function (atom) {
			//logger.debug('Sending sms result ' + atom);
		});
	}
}

function send_alarm_log_to_bs(log, uuid, event_count)
{
	var history_log = new Object();

	history_log.uuid = uuid;
	history_log.rule_id = log.rule_id;
	history_log.alarmtime = Math.floor(log.log_time / 1000);
	history_log.project_name = log.project_name;
	history_log.project_version = log.project_version;
	history_log.host = log.host;
	history_log.event_count = event_count;
	history_log.mail = rules[log.rule_id].mail;
	history_log.mobile= rules[log.rule_id].mobile;
	history_log.log_msg= log.log_msg;

	logger.debug("history log=" + JSON.stringify(history_log));

	/* send alarm log to Business Server */
	var mysql_conn = mysql.createConnection(mysql_config);
	mysql_conn.connect();
	
	var query = util.format('INSERT INTO alert_history VALUES ' +
                            '(\'%s\',\'%d\',NOW(),FROM_UNIXTIME(%d),\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\')',
							history_log.uuid, history_log.rule_id, history_log.alarmtime, 
							history_log.project_name, history_log.project_version, history_log.host, 
                            history_log.event_count, history_log.mail, history_log.mobile, history_log.log_msg);
	mysql_conn.query(query, function(err, rows, fields) {
		if (err != null) {
			logger.error(query);
			logger.error(err);
		}
		mysql_conn.end();
	});
}

exports.store_log = function(logs) {

	logger.debug("received log:", JSON.stringify(logs));

	_.each(logs.rule_id, function(rule_id) {
		var log = new Object();

		log.rule_id = rule_id;

		if ( rules[log.rule_id] == undefined ) {
			logger.error("rule id = %d is invalid rule number", log.rule_id);
		} else {
			log.log_time = logs.log_time;
			log.project_name = logs.project_name;
			log.project_version = logs.project_version == null ? '' : logs.project_version; 
			log.host = logs.host; 
			try {
				//log.log_msg = decodeURIComponent(logs.log_msg.substring(0,255));		// compatible with nelo1
				log.log_msg = decodeURI(logs.log_msg);
			} catch (e) {
				logger.error(e);
			}
			log.log_msg = log.log_msg.substring(0,255);		// compatible with nelo1
			log.event_count = logs.event_count;
			log.fields = logs.fields;

			/* if threshold is 1 and no alarm key , send alarm at once */
			if (rules[rule_id].threshold == 1) {
				var alarm_key = "alarm:"+log.rule_id+":"+log.project_version+":"+log.log_msg;
				
				try {
					if ( rules[rule_id].alarm_each_host == 'Y' ||
						 rules[rule_id].alarm_merge_host == 'Y' ) {
						log_key += ":"+log.host;
					}
				} catch (e) {
						logger.error(e);
						logger.error(log);
						logger.error(rules[rule_id]);
						return;
				}

				client.hgetall(alarm_key, function (err, alarm_reply) {
					if ( err != null ) {
						logger.error("client.keys alarm: " + err );
					} else if ( alarm_reply === null ) {
						// Send alarm 
						send_log_alarmcenter(log, 1, rules[rule_id], -1);

						client.hset(alarm_key,
									 'alarm_time', current_time, function (err) {
							if ( err != null ) {
								logger.error("hmset alarm: "+ err);
							}
						});

						var expire_min = Number(rules[rule_id].alarm_interval[0]);
						client.expire(alarm_key, expire_min*60, function (err, reply) {
							if ( err != null ) {
								logger.error("client.expire " + key + err );
							} else {
								logger.info("alarm:" + rule_id + " stored for " + expire_min*60 + "s");
							}
						});
					} else {
						logger.debug(alarm_key + " - this log is duplicated");
					}
				});
			} else {
				save_log_table(log);
			}
		}
	});
}

function save_log_table(log) {

	var log_key = "log:"+log.rule_id+":"+log.project_version+":"+log.log_msg;
	
	try {
		if ( rules[log.rule_id].alarm_each_host == 'Y' ||
			 rules[log.rule_id].alarm_merge_host == 'Y' ) {
			log_key += ":"+log.host;
		}
	} catch (e) {
			logger.error(e);
			logger.error(log);
			logger.error(rules[log.rule_id]);
			return;
	}

	client.hincrby(log_key, "event_count", log.event_count, function (err, reply) {
		if ( err != null ) {
			logger.error("client.hincrby " + log_key + " error: " + err );
		} else {
			client.hset(log_key, "log", JSON.stringify(log), function (err, reply) {
				if ( err != null ) {
					logger.error("client.hset " + log_key + " error: " + err );
				} 
			});
		}
	});
}

function send_log_alarmcenter(log, event_count, rule, alarm_interval)
{
	uuid = node_uuid.v4();

	/* step 2: send sms or email */
	if ( rule.mail != undefined && rule.mail.length > 1 ) {
		save_alarm_email_to_redis(log, rule, uuid, alarm_interval);
	}
	if ( rule.mobile != undefined && rule.mobile.length > 1 ) {
		send_alarm_to_sms(log, rule, alarm_interval);
	}

	/* step 3: save alarm log to business server */
	send_alarm_log_to_bs(log, uuid, event_count);
}

function backup_log(logkey) {
	var log_backup_key = "log_backup"+logkey.substring(3);
	client.rename(logkey, log_backup_key, function(err) {
		if ( err != null ) {
			logger.error("rename key:"+ err);
		} else {
			client.expire(log_backup_key, 60*60*24,function(err) {
				if ( err != null ) {
					logger.error("expire key:"+ err);
				}
			});
		}
	});
}

exports.cron_checklog = function() {
	current_time = new Date(); // milliseconds
	current_time = current_time / 1000;
	current_time = Math.floor(current_time / 60) * 60;
	current_time = current_time * 1000; // milliseconds
	var cur_minute   = (new Date()).getMinutes();
	var results = [];
	
	client.keys("log:*", function (err, log_reply) {
		if ( err != null ) {
			logger.error("client.keys log: " + err );
			return null;
		} else if ( log_reply.length == 0 ) {
			logger.debug("no log:*");
			return null;
		} 

		_.each(log_reply, function (logkey) {
			client.hgetall(logkey, function(err, reply) {
				if ( err != null ) {
					logger.error("client.hgetall logkey: " + err );
					check_merge_log(results.push(logkey), log_reply.length);;
				} else if ( reply === null ) {
					logger.debug("no " + logkey);
					check_merge_log(results.push(logkey), log_reply.length);;
				} else if ( rules[logkey.split(':')[1]] == undefined ) {
					/* invalid rule / log */
					client.del(logkey, function(err) {
						if ( err != null ) {
							logger.error("del key:"+ err);
						} else {
							logger.debug("del key:"+ logkey);
						}
					});
					check_merge_log(results.push(logkey), log_reply.length);;
				} else if ( (cur_minute % rules[logkey.split(':')[1]].alarm_interval[0]) != 0 ) {
					//logger.debug("alarm interval[0] = " + rules[logkey.split(':')[1]].alarm_interval[0]);
					check_merge_log(results.push(logkey), log_reply.length);;
				} else {

					backup_log(logkey);

					var rule_id  = logkey.split(':')[1];
					var proj_ver = logkey.split(':')[2];
					var log_msg  = logkey.split(':')[3];
					var host     = logkey.split(':')[4];
					var event_count = reply['event_count'];
					var loginfo     = eval('('+reply['log']+')');

					if (rules[rule_id].threshold <= event_count) {
						var alarm_key = "alarm" + logkey.substring(3);
						async.waterfall([
							function(cb) {
								client.hgetall(alarm_key, function (err, alarm_reply) {
									if ( err != null ) {
										logger.error("client.keys alarm: " + err );
										cb(-1, 0);
									} else if ( alarm_reply === null ) {
										logger.debug("no " + alarm_key);
										cb(null, 0);
									} else {
										/* Alarm Interval compare with log time */
										prev_alarm_time = alarm_reply['alarm_time'];
										prev_alarm_index = alarm_reply['alarm_time_idx'];

										if (rules[rule_id].alarm_interval.length > prev_alarm_index+1) {
											prev_alarm_index++;
										}

										if (Number(prev_alarm_time)/1000 + 
											Number(rules[rule_id].alarm_interval[prev_alarm_index]*60) 
												<= Number(current_time)/1000 ) {
											cb(null, prev_alarm_index);
										} else {
											logger.debug(logkey + " - this alarm log is duplicated");
											cb(-1, 0);
										}
									}
								});
							},
							function(alarm_index, cb) {
								if ( rules[rule_id].alarm_merge_host == 'Y' ) {
									/* Save log to merge host alarm key-value (redis) */
									logger.info("Save log to merge host alarm key-value (redis)");
									alarm_merge_key = util.format("alarm_merge_host:%s:%s:%s", rule_id, proj_ver, log_msg);
									client.hmset(alarm_merge_key, 
													'alarm_interval', rules[rule_id].alarm_interval[alarm_index], 
													'event_count', event_count, 
													'last_log_time', loginfo['log_time'], 
													function (err) {
										if ( err != null ) {
											logger.error("hmset alarm_merge_key:"+ err);
										} else {
											client.hincrby(alarm_merge_key,
															loginfo['host'], event_count, function (err) {
												if ( err != null ) {
													logger.error("hincrby alarm_merge_key:"+ err);
												}
											});
										}
										cb(null, 0);
									});
								} else {
									// Send alarm 
									send_log_alarmcenter(loginfo, event_count, rules[rule_id], 
														 rules[rule_id].alarm_interval[alarm_index]);

									// Store Alarm log
									if (Number(rules[rule_id].alarm_interval.length) > Number(alarm_index)+1) {
										alarm_index = Number(alarm_index) + 1;
										logger.info(alarm_index);
									}
									client.hmset(alarm_key,
												 'alarm_time', current_time,
												 'alarm_time_idx', alarm_index, function (err) {
										if ( err != null ) {
											logger.error("hmset alarm: "+ err);
										}
									});

									var expire_min = Number(rules[rule_id].alarm_interval[alarm_index]);
									client.expire(alarm_key, expire_min*60, function (err, reply) {
										if ( err != null ) {
											logger.error("client.expire " + key + err );
										} else {
											logger.info("alarm:" + rule_id + " stored for " + expire_min*60 + "s");
										}
									});
									cb(null, 0);
								}
							}
						], function (err, result) {
							check_merge_log(results.push(logkey), log_reply.length);;
						});
					} else {
						check_merge_log(results.push(logkey), log_reply.length);;
					}
				}
			});
		});
	});
};

function check_merge_log(results, length)
{
	if ( results != length ) {
		return ;
	}

	client.keys("alarm_merge_host:*", function (err, reply) {
		if ( err != null ) {
			logger.error("client.keys alarm_merge_host: " + err );
			return null;
		} else if ( reply.length == 0 ) {
			logger.debug("no keys - alarm_merge_host:*");
			return null;
		} 

		// logkey format : alarm_merge_host:{rule_id}:{proj_ver}:{log_msg}
		// alarm_merge_host keys are : alarm_interval, error_count, last_log_time, {host} 
		_.each(reply, function (logkey) {
			client.hgetall(logkey, function(err, reply) {
				
				if ( err != null ) {
					logger.error("client.hgetall " + logkey + " " + err );
					return null;
				} else if ( reply === null ) {
					logger.debug("no " + logkey);
					return null;
				} 


				logger.info("send alarm !!");
				logger.info(reply);
				send_mergehost_alarmcenter(reply, logkey);

				client.del(logkey, function(err) {
					if ( err != null ) {
						logger.error("del key:"+ err);
					} else {
						logger.debug("del key:"+ logkey);
					}
				});
			});
		});
	});
};

function send_mergehost_alarmcenter(log_info, logkey)
{
	var uuid     = node_uuid.v4();
	var rule_id  = logkey.split(':')[1];
	var proj_ver = logkey.split(':')[2];
	var log_msg  = logkey.split(':')[3];
	var rule     = rules[rule_id];

	/* step 2: send sms or email */
	if ( rule.mail != undefined && rule.mail.length > 1 ) {
		send_mergealarm_to_email(rule, uuid, log_info, proj_ver, log_msg);
	}
	if ( rule.mobile != undefined && rule.mobile.length > 1 ) {
		send_mergealarm_to_sms(rule, uuid, log_info, proj_ver, log_msg);
	}

	/* step 3: save alarm log to business server, delete log in redis */
	send_mergealarm_log_to_bs(rule, uuid, log_info, proj_ver, log_msg);
}

function send_mergealarm_to_email(rule, uuid, log_info, proj_ver, log_msg) 
{
	var email_subject, email_text;

	// Create a SMTP transport object
	var transport = nodemailer.createTransport("SMTP", {
		host: "smail01.nhncorp.com", // hostname
		port: 25
	});

	email_subject = rule.project_name;
	if ( proj_ver != undefined && proj_ver.length > 0 ) {
		email_subject += ' (v' + proj_ver + ')';
	}
	email_subject += ' in the last ' + log_info.alarm_interval + ' minutes, ';
	email_subject += log_msg.substring(0,255) + ' errors occurred.';

	email_text = email_subject + '\n';
	email_text += "Host Info :\n";
	for (host in log_info) {
		if ( host != 'alarm_interval' && host != 'last_log_time' && host != 'event_count' ) {
			email_text += "            " + host + " ( event count " + log_info[host] + " )\n";
		}
	}
	email_text += '\nGo to nelo2 page : http://nlog.nhncorp.com/logview/' + uuid;

	// Message object
	var message = { 
		// sender info
		from: 'NELO Admin <nelo-admin@nhn.com>',

		// Comma separated list of recipients
		to: "sungduk.yu@nhn.com",
//		to: rule.mail,

		// Subject of the message
		subject: '[NELO Alert] ' + email_subject,

		// plaintext body
		text: email_text
	};

	//logger.info('Sending Mail:' + rule.mail);
	logger.debug('[TEST] Sending Mail:sungduk.yu@nhn.com');
	logger.debug('[TEST] '+email_text);
	transport.sendMail(message, function(error){
		if(error){
			logger.error('send mail Error occured:', error.message);
			return;
		}
		logger.debug('Message sent successfully!');

		// if you don't want to use this transport object anymore, uncomment following line
		transport.close(); // close the connection pool
	});
}

function send_mergealarm_to_sms(rule, uuid, log_info, proj_ver, log_msg)
{
	var sms_text;

	sms_text = rule.project_name;
	if ( proj_ver != undefined && proj_ver.length > 0 ) {
		sms_text += ' (v' + proj_ver + ')';
	}

	sms_text += ' in the last ' + log_info.alarm_interval + ' minutes, ';
	sms_text += log_msg.substring(0,255) + ' errors occurred.';
	sms_text += " Host: ";
	for (host in log_info) {
		if ( host != 'alarm_interval' && host != 'last_log_time' && host != 'event_count' ) {
			sms_text += util.format("%s(%d) ", host, log_info[host]);
		}
	}

	var test_phone = rule.mobile;
	mobile_list = test_phone.split(",");
	for (idx in mobile_list) {
		mobile = mobile_list[idx].split(" ").join("");
		var mex_call = util.format("/mex-mt-server/SendBO/sendSMS?serviceId=\"ESM00200\"&sendMdn=\"%s\"&receiveMdnList=[\"%s\"]&content=\"%s\"", mobile, mobile, sms_text);
		logger.debug(mex_call);

		logger.info('Sending sms ' + rule.mobile);

		send_text_to_mex( mex_call, function (atom) {
			//logger.debug('Sending sms result ' + atom);
		});
	}
}

function send_mergealarm_log_to_bs(rule, uuid, log_info, proj_ver, log_msg)
{
	var history_log = new Object();
	var event_count = 0;

	history_log.uuid = uuid;
	history_log.rule_id = rule.rule_id;
	history_log.last_log_time = Math.floor(log_info.last_log_time / 1000);
	history_log.project_name = rule.project_name;
	history_log.project_version = proj_ver;
	for (host in log_info) {
		event_count += log_info[host];
		if ( history_log.host == undefined )
			history_log.host = host;
		else
			history_log.host += ", " + host;
	}
	history_log.event_count = event_count;
	history_log.mail = rule.mail;
	history_log.mobile= rule.mobile;

	logger.info("history log=" + JSON.stringify(history_log));
	/* Send alarm log to Business Server */
	var mysql_conn = mysql.createConnection(mysql_config);
	mysql_conn.connect();

	var query = util.format('INSERT INTO alert_history VALUES ' +
                            '(\'%s\',\'%d\',NOW(),FROM_UNIXTIME(%d),\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\',\'%s\')',
							history_log.uuid, history_log.rule_id, history_log.last_log_time, 
							history_log.project_name, history_log.project_version, history_log.host, 
                            history_log.event_count, history_log.mail, history_log.mobile,log_msg);
	mysql_conn.query(query, function(err, rows, fields) {
		if (err != null) {
			logger.error(query);
			logger.error(err);
		}
		mysql_conn.end();
	});
}

exports.get_rules_from_db = function() {
	var mysql_conn = mysql.createConnection(mysql_config);
	mysql_conn.connect();

	var query = 'SELECT r.id id, p.projectname project_name, r.mobile mobile, ' + 
				'r.mail mail, r.threshold threshold, r.alarm_interval alarm_interval, ' +
				'r.alarm_each_host alarm_each_host, r.merge_host_alarm merge_host_alarm ' + 
				'from project_realtime_alert r left outer join project p on r.project_id = p.id';
	mysql_conn.query(query, function(err, rows, fields) {
		if (err != null) {
			logger.error(err);
		}

		var new_rules = new Object();
		var rules_count = 0;

		logger.debug("Get %d rules.", rows.length);
		if ( rows.length <= 0 ) {
			mysql_conn.end();
			return ;
		}

		_.each(rows, function(rule) {
			new_rules[rule.id] = new Object();
			new_rules[rule.id].rule_id = rule.id;
			new_rules[rule.id].project_name = rule.project_name;
			new_rules[rule.id].mobile = rule.mobile;
			new_rules[rule.id].mail = rule.mail
			new_rules[rule.id].threshold = rule.threshold;
			new_rules[rule.id].alarm_interval = eval(rule.alarm_interval);
			new_rules[rule.id].alarm_each_host = rule.alarm_each_host;
			new_rules[rule.id].alarm_merge_host = rule.merge_host_alarm;

			if (++rules_count == rows.length) {
				mysql_conn.end();
				rules = new_rules;
				logger.debug("Rules sync completed (%d)", rules_count);
				logger.trace(rules);
			}
		});
	});
};

exports.set_loglevel = function(loglevel) {
	if ( loglevel == undefined ) {
		logger.error("log level string(%s) is invalid.", loglevel);
		return ;
	}
	logger.setLevel(loglevel);

	try {
		if ( loglevel.toLowerCase() == 'trace' ) {
			logger.trace('log level is changed to TRACE');
		} else if ( loglevel.toLowerCase() == 'debug' ) {
			logger.debug('log level is changed to DEBUG');
		} else if ( loglevel.toLowerCase() == 'info' ) {
			logger.info('log level is changed to INFO');
		} else if ( loglevel.toLowerCase() == 'warn' ) {
			logger.warn('log level is changed to WARN');
		} else if ( loglevel.toLowerCase() == 'error' ) {
			logger.error('log level is changed to ERROR');
		} else if ( loglevel.toLowerCase() == 'fatal' ) {
			logger.fatal('log level is changed to FATAL');
		} else {
			logger.error("log level string(%s) is invalid.", loglevel);
		}
	} catch (e) {
		logger.error(e);
	}
};
