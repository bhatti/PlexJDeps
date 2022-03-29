package com.plexobject.demo.dao;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.plexobject.demo.ddb.CatalogItem;
import com.plexobject.demo.ddb.ProductCatalogItem;

public class DDBMapper {
    private AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
    private DynamoDBMapper mapper = new DynamoDBMapper(client);

    public void saveCatalog() {
        CatalogItem item = mapper.load(CatalogItem.class, 101);
        item.setTitle("This is a new title for the item");
        mapper.save(item,
                new DynamoDBMapperConfig(
                        DynamoDBMapperConfig.SaveBehavior.CLOBBER));
    }

    public void saveProduct() {
        ProductCatalogItem item = new ProductCatalogItem();
        ProductCatalogItem.Pictures pix = new ProductCatalogItem.Pictures();
        pix.setFrontView("http://example.com/products/123_front.jpg");
        pix.setRearView("http://example.com/products/123_rear.jpg");
        pix.setSideView("http://example.com/products/123_left_side.jpg");
        item.setPictures(pix);
        item.setId(System.currentTimeMillis());
        mapper.save(item);
    }
}
