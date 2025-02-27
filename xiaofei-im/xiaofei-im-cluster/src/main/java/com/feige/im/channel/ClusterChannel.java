package com.feige.im.channel;

import com.feige.im.constant.ChannelAttr;
import com.feige.im.log.Logger;
import com.feige.im.log.LoggerFactory;
import com.feige.im.utils.StringUtil;
import com.google.protobuf.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.Attribute;


import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author feige<br />
 * @ClassName: ClusterChannel <br/>
 * @Description: 专门用来管理集群之间用来通信的Channel<br/>
 * @date: 2021/11/6 12:30<br/>
 */
public class ClusterChannel {

    private static final Logger LOG = LoggerFactory.getLogger();

    private static final Map<String, Channel> MAP = new ConcurrentHashMap<>();
    // 监听连接关闭
    private transient final ChannelFutureListener remover = future -> {
        future.removeListener(INSTANCE.remover);
        remove(future.channel());
    };

    private static final ClusterChannel INSTANCE = new ClusterChannel();

    private ClusterChannel(){

    }


    public static ClusterChannel getInstance() {
        return INSTANCE;
    }

    /**
     * @description: 获取节点的唯一标识
     * @author: feige
     * @date: 2021/10/9 21:10
     * @param	channel	当前用户的通道
     * @return: java.lang.String
     */
    public String getKey(Channel channel){
        Attribute<String> attr = channel.attr(ChannelAttr.NODE_KEY);
        if (!Objects.isNull(attr)){
            return attr.get();
        }
        return null;
    }

    /**
     * @description: 把channel保存到MAP中
     * @author: feige
     * @date: 2021/10/9 22:08
     * @param	channel
     * @return: void
     */
    public void add(Channel channel){
        String key = getKey(channel);
        if (StringUtil.isEmpty(key) || !channel.isActive()){
            LOG.warn("nodeKey={}的连接未绑定节点ID，添加失败！",key);
            return;
        }
        channel.closeFuture().addListener(remover);
        MAP.put(key,channel);
        LOG.debugInfo("nodeKey={}的连接已加入管理，当前共管理{}个连接",key, MAP.size());
    }

    public void remove(Channel channel){
        String key = getKey(channel);
        if (StringUtil.isEmpty(key) || !channel.isActive()){
            return;
        }
        MAP.remove(key);
        LOG.debugInfo("nodeKey={}的连接从管理中移除，当前共管理{}个连接",key, MAP.size());
    }



    /**
     * @description: 往对应key的通道中写数据
     * @author: feige
     * @date: 2021/10/9 22:11
     * @param	key
     * @param	msg
     * @return: void
     */
    public void write(String key, Message msg){
        LOG.debugInfo("msg={}的消息开始转发到nodeKey={}的节点", StringUtil.protoMsgFormat(msg),key);
        Channel channel = MAP.get(key);
        if (StringUtil.isEmpty(channel)){
            LOG.warn("nodeKey={}的连接未发现在集合中，消息转发失败！",key);
            return;
        }
        channel.writeAndFlush(msg);
        LOG.debugInfo("msg={}的消息已转发到nodeKey={}的节点", StringUtil.protoMsgFormat(msg), key);
    }

    public boolean containsKey(String nodeKey){
        return MAP.containsKey(nodeKey);
    }
}
