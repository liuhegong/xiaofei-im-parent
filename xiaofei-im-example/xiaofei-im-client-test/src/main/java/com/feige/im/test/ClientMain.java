package com.feige.im.test;

import com.feige.im.client.ImClient;
import com.feige.im.parser.Parser;
import com.feige.im.pojo.proto.DefaultMsg;
import io.netty.channel.Channel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author feige<br />
 * @ClassName: Main <br/>
 * @Description: <br/>
 * @date: 2021/11/6 23:19<br/>
 */
public class ClientMain {

    public static void main(String[] args) throws IOException {
        Parser.registerDefaultParsing();
        ImClient localhost = ImClient.connect("127.0.0.1", 8001, (key, channel, msg,throwable) -> {
            System.out.println(msg);
        });
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        while (true){
            String s = bufferedReader.readLine();
            Channel channel = localhost.getChannel();
            if ("1".equals(s)){
                DefaultMsg.Auth auth = DefaultMsg.Auth.newBuilder()
                        .setPlatform("android")
                        .setToken("0")
                        .setDeviceName("w")
                        .setDeviceId("1")
                        .setLanguage("en")
                        .setOsVersion("1.0").setVersion("1.0")
                        .setAddress("四川省成都市双流区")
                        .setIp("10.1.204.70")
                        .build();
                channel.writeAndFlush(auth);
            }else {
                DefaultMsg.Msg msg = DefaultMsg.Msg.newBuilder()
                        .setId(123456L)
                        .setContent("hello netty!")
                        .setMsgType(1)
                        .setExtra("extra")
                        .setFormat(1)
                        .setSenderId("0")
                        .setReceiverId("1")
                        .setStatus(1)
                        .setTimestamp(String.valueOf(System.currentTimeMillis()))
                        .build();
                DefaultMsg.TransportMsg transportMsg = DefaultMsg.TransportMsg.newBuilder()
                        .setType(1)
                        .setMsg(msg)
                        .build();
                channel.writeAndFlush(transportMsg);
            }

        }
    }
}
