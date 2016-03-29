package model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: peterdietz
 * Date: 8/10/12
 * Time: 11:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class Item {
    /*
    bitstreams (complex)
    bundles (complex)
    collections - list
    communities -
    lastModified datestamp
    owningCollection - detail
     */
    
    public Long id;
    public String name;
    public List<MetadataField> metadata = new ArrayList<MetadataField>();

    public List<Bitstream> bitstreams  = new ArrayList<Bitstream>();

    public String handle;
    public boolean isArchived, isWithdrawn;
    public String submitterFullName;
    
    public List<Collection> collections = new ArrayList<Collection>();
    public List<Community>  communities = new ArrayList<Community>();


    public static Item parseItemFromJSON(JsonNode itemNode,Institution inst) {
        Item item = new Item();

        item.id = itemNode.get("id").asLong();
        item.name = itemNode.get("name").asText();

        item.handle = itemNode.get("handle").asText();

        if(itemNode.has("archived")) {
            item.isArchived = itemNode.get("archived").asBoolean();
        }

        //item.isWithdrawn = itemNode.get("isWithdrawn").asBoolean();

        //item.submitterFullName = itemNode.get("submitter").get("fullName").asText();

        if(itemNode.has("metadata")) {
            JsonNode metadataNode = itemNode.get("metadata");
            item.metadata = new ArrayList<MetadataField>();

            for(JsonNode field : metadataNode) {
                String key = field.get("key").asText();
                String value = field.get("value").asText();
                int idx = inst.metafields.indexOf(key);
                if (idx >=0) {
                    item.metadata.add(new MetadataField(key, value, idx));
                }
            }
            Collections.sort(item.metadata);
        }

        if(itemNode.has("bitstreams")) {
            item.bitstreams = new ArrayList<Bitstream>();
            JsonNode bitstreamsNode = itemNode.get("bitstreams");
            for(JsonNode bitstreamNode : bitstreamsNode) {
                Bitstream bitstream = Bitstream.parseBitstreamFromJSON(bitstreamNode);
                bitstream.link =  item.handle + "/" + bitstream.name + "?sequence=" + bitstream.sequence + "&isAllowed=y";
                item.bitstreams.add(bitstream);
            }
        }

        if(itemNode.has("parentCollectionList")) {
            JsonNode collectionNodes = itemNode.get("parentCollectionList");
            for(JsonNode collectionNode : collectionNodes) {
                Collection collection = Collection.parseCollectionFromJSON(collectionNode);
                item.collections.add(collection);
            }
        }

        if(itemNode.has("parentCommunityList")) {
            JsonNode communityNodes = itemNode.get("parentCommunityList");
            for(JsonNode communityNode : communityNodes) {
                Community community = Community.parseCommunityFromJSON(communityNode);
                item.communities.add(community);
            }
        }

        return item;
    }

    public String getDescription() {
       for (MetadataField meta : metadata) {
           if (meta.getKey().equals("dc.description") && meta.getValue() != null && !meta.getValue().equals("")) {
               return meta.getValue();
           }
       }
        return null;
    }
    public String getDate() {
        for (MetadataField meta : metadata) {
            if (meta.getKey().equals("dc.date") && meta.getValue() != null && !meta.getValue().equals("")) {
                return meta.getValue();
            }
        }
        return null;
    }

    public String getIssueDate() {
        for (MetadataField meta : metadata) {
            if (meta.getKey().equals("dc.date.issued") && meta.getValue() != null && !meta.getValue().equals("")) {
                return meta.getValue();
            }
        }
        return null;
    }
}
