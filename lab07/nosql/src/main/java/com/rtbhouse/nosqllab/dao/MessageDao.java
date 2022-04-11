package com.rtbhouse.nosqllab.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.avro.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.schema.registry.client.SchemaRegistryClient;
import org.springframework.stereotype.Component;

import com.aerospike.client.AerospikeClient;
import com.aerospike.client.Bin;
import com.aerospike.client.Host;
import com.aerospike.client.Key;
import com.aerospike.client.Language;
import com.aerospike.client.Record;
import com.aerospike.client.lua.LuaConfig;
import com.aerospike.client.policy.ClientPolicy;
import com.aerospike.client.policy.CommitLevel;
import com.aerospike.client.policy.Policy;
import com.aerospike.client.policy.RecordExistsAction;
import com.aerospike.client.policy.Replica;
import com.aerospike.client.policy.WritePolicy;
import com.aerospike.client.query.ResultSet;
import com.aerospike.client.query.Statement;
import com.aerospike.client.task.RegisterTask;
import com.rtbhouse.nosqllab.Message;
import com.rtbhouse.nosqllab.avro.SerDe;
import com.rtbhouse.nosqllab.schema.SchemaVersion;

@Component
public class MessageDao {

    private static final String NAMESPACE = "mimuw";
    private static final String SET = "messages";
    private static final String MESSAGE_BIN = "message";
    private static final String VERSION_BIN = "version";

    private AerospikeClient client;
    private SerDe<Message> serde;

    @Autowired
    private SchemaVersion schemaVersion;

    @Autowired
    private SchemaRegistryClient schemaRegistryClient;

    private static ClientPolicy defaultClientPolicy() {
        ClientPolicy defaulClientPolicy = new ClientPolicy();
        defaulClientPolicy.readPolicyDefault.replica = Replica.MASTER_PROLES;
        defaulClientPolicy.readPolicyDefault.socketTimeout = 100;
        defaulClientPolicy.readPolicyDefault.totalTimeout = 100;
        defaulClientPolicy.writePolicyDefault.socketTimeout = 15000;
        defaulClientPolicy.writePolicyDefault.totalTimeout = 15000;
        defaulClientPolicy.writePolicyDefault.maxRetries = 3;
        defaulClientPolicy.writePolicyDefault.commitLevel = CommitLevel.COMMIT_MASTER;
        defaulClientPolicy.writePolicyDefault.recordExistsAction = RecordExistsAction.REPLACE;
        return defaulClientPolicy;
    }

    public MessageDao(@Value("${aerospike.seeds}") String[] aerospikeSeeds, @Value("${aerospike.port}") int port) {
        this.client = new AerospikeClient(defaultClientPolicy(), Arrays.stream(aerospikeSeeds).map(seed -> new Host(seed, port)).toArray(Host[]::new));
        this.serde = new SerDe<>(Message.getClassSchema());
    }

    @PostConstruct
    public void registerUdfs() throws IOException {
        LuaConfig.SourceDirectory = "src/main/resources/udf/";
        RegisterTask task = client.register(null, "src/main/resources/udf/count.lua", "count.lua", Language.LUA);
        task.waitTillComplete(1000, 10000);
    }

    public void put(Message message) {
        WritePolicy writePolicy = new WritePolicy(client.writePolicyDefault);

        Key key = new Key(NAMESPACE, SET, String.valueOf(message.getId()));
        Bin versionBin = new Bin(VERSION_BIN, schemaVersion.getCurrentSchemaVersion());
        Bin messageBin = new Bin(MESSAGE_BIN, serde.serialize(message));
        client.put(writePolicy, key, versionBin, messageBin);
    }

    public Message get(long id) {
        Policy readPolicy = new Policy(client.readPolicyDefault);

        Key key = new Key(NAMESPACE, SET, String.valueOf(id));
        Record record = client.get(readPolicy, key, VERSION_BIN, MESSAGE_BIN);

        if (record == null) {
            return null;
        }

        int writerSchemaId = record.getInt(VERSION_BIN);
        if (schemaVersion.getCurrentSchemaVersion() == writerSchemaId) {
            return serde.deserialize((byte[]) record.getValue(MESSAGE_BIN), Message.getClassSchema());
        }

        String schema = schemaRegistryClient.fetch(writerSchemaId);
        if (schema == null) {
            throw new IllegalStateException("Schema with id: " + writerSchemaId + " does not exist");
        }

        return serde.deserialize((byte[]) record.getValue(MESSAGE_BIN), new Schema.Parser().parse(schema));
    }

    public Long count() {
        Statement statement = new Statement();
        statement.setNamespace(NAMESPACE);
        statement.setSetName(SET);

        try (ResultSet resultSet = client.queryAggregate(null, statement, "count", "count")) {
            Iterator<Object> iterator = resultSet.iterator();
            if (iterator.hasNext()) {
                return (Long) iterator.next();
            }
            return 0L;
        }
    }

    @PreDestroy
    public void close() {
        client.close();
    }
}
