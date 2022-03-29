package com.plexobject.demo.ddb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.plexobject.aop.Tracer;

@Tracer
public class ProductCatalogItem {
    private Long id;
    private Pictures pictures;

    @DynamoDBHashKey(attributeName = "Id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @DynamoDBAttribute(attributeName = "Pictures")
    public Pictures getPictures() {
        return pictures;
    }

    public void setPictures(Pictures pictures) {
        this.pictures = pictures;
    }

    @DynamoDBDocument
    public static class Pictures {
        private String frontView;
        private String rearView;
        private String sideView;

        @DynamoDBAttribute(attributeName = "FrontView")
        public String getFrontView() {
            return frontView;
        }

        public void setFrontView(String frontView) {
            this.frontView = frontView;
        }

        @DynamoDBAttribute(attributeName = "RearView")
        public String getRearView() {
            return rearView;
        }

        public void setRearView(String rearView) {
            this.rearView = rearView;
        }

        @DynamoDBAttribute(attributeName = "SideView")
        public String getSideView() {
            return sideView;
        }

        public void setSideView(String sideView) {
            this.sideView = sideView;
        }
    }
}