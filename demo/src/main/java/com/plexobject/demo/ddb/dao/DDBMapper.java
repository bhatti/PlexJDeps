package com.plexobject.demo.ddb.dao;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.*;
import com.plexobject.aop.Tracer;
import com.plexobject.demo.ddb.model.CatalogItem;
import com.plexobject.demo.ddb.model.MusicItems;

import java.util.ArrayList;
import java.util.List;

public class DDBMapper {
    private final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private final DynamoDBMapper mapper = new DynamoDBMapper(client);

    public void createMusicTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(
                        new AttributeDefinition("Artist", ScalarAttributeType.S),
                        //new AttributeDefinition("AlbumTitle", ScalarAttributeType.S),
                        //new AttributeDefinition("Awards", ScalarAttributeType.N),
                        new AttributeDefinition("SongTitle", ScalarAttributeType.S))
                .withKeySchema(
                        new KeySchemaElement("Artist", KeyType.HASH),
                        new KeySchemaElement("SongTitle", KeyType.RANGE))
                .withProvisionedThroughput(
                        new ProvisionedThroughput(10L, 10L))
                .withTableName("Music");
        try {
            CreateTableResult result = client.createTable(request);
            System.out.println(result.getTableDescription().getTableName());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    public void createCatalogTable() {
        CreateTableRequest request = new CreateTableRequest()
                .withAttributeDefinitions(
                        new AttributeDefinition("Id", ScalarAttributeType.S))
                //new AttributeDefinition("Title", ScalarAttributeType.S),
                //new AttributeDefinition("ISBN", ScalarAttributeType.S),
                //new AttributeDefinition("Authors", ScalarAttributeType.S),
                //new AttributeDefinition("Version", ScalarAttributeType.N))
                .withKeySchema(new KeySchemaElement("Id", KeyType.HASH))
                .withProvisionedThroughput(new ProvisionedThroughput(10L, 10L))
                .withTableName("ProductCatalog");

        try {
            CreateTableResult result = client.createTable(request);
            System.out.println(result.getTableDescription().getTableName());
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
        }
    }

    @Tracer
    public MusicItems saveMusic() {
        MusicItems items = new MusicItems();
        items.setArtist("artist1" + System.currentTimeMillis());
        items.setSongTitle("songTitle1");
        items.setAlbumTitle("albumTitle1");
        items.setAwards(11);
        updateMusic(items);
        return items;
    }

    @Tracer
    public MusicItems updateMusic(MusicItems items) {
        try {
            mapper.save(items);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return items;
    }

    @Tracer
    public MusicItems loadMusic(String artistName, String songQueryTitle) {
        return mapper.load(MusicItems.class, artistName, songQueryTitle);
    }

    public long countMusic() {
        return mapper.count(MusicItems.class, new DynamoDBScanExpression());
    }

    public void describeTable(String table) {
        TableDescription table_info = client.describeTable(table).getTable();
        if (table_info != null) {
            System.out.format("Table name  : %s\n",
                    table_info.getTableName());
            System.out.format("Table ARN   : %s\n",
                    table_info.getTableArn());
            System.out.format("Status      : %s\n",
                    table_info.getTableStatus());
            System.out.format("Item count  : %d\n",
                    table_info.getItemCount());
            System.out.format("Size (bytes): %d\n",
                    table_info.getTableSizeBytes());

            ProvisionedThroughputDescription throughput_info =
                    table_info.getProvisionedThroughput();
            System.out.println("Throughput");
            System.out.format("  Read Capacity : %d\n",
                    throughput_info.getReadCapacityUnits());
            System.out.format("  Write Capacity: %d\n",
                    throughput_info.getWriteCapacityUnits());

            List<AttributeDefinition> attributes =
                    table_info.getAttributeDefinitions();
            System.out.println("Attributes");
            for (AttributeDefinition a : attributes) {
                System.out.format("  %s (%s)\n",
                        a.getAttributeName(), a.getAttributeType());
            }
        }
    }

    public List<String> getTables() {
        ListTablesRequest request;
        boolean more_tables = true;
        String last_name = null;
        List<String> tables = new ArrayList<>();
        while (more_tables) {
            try {
                if (last_name == null) {
                    request = new ListTablesRequest().withLimit(10);
                } else {
                    request = new ListTablesRequest()
                            .withLimit(10)
                            .withExclusiveStartTableName(last_name);
                }

                ListTablesResult table_list = client.listTables(request);
                List<String> table_names = table_list.getTableNames();

                if (table_names.size() > 0) {
                    tables.addAll(table_names);
                }
                last_name = table_list.getLastEvaluatedTableName();
                if (last_name == null) {
                    more_tables = false;
                }
            } catch (AmazonServiceException e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return tables;
    }

    @Tracer
    public CatalogItem saveCatalog(CatalogItem item) {
        try {
            mapper.save(item);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        return item;
    }

    @Tracer
    public long countCatalog() {
        return mapper.count(CatalogItem.class, new DynamoDBScanExpression());
    }

    @Tracer
    public CatalogItem loadCatalog(String id) {
        return mapper.load(CatalogItem.class, id);
    }
}
