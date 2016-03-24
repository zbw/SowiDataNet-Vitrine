package model;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import play.i18n.Messages;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: peterdietz
 * Date: 10/2/13
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class Bitstream {
    public String name, description, format, mimeType, bundleName, retrieveLink, checksum, link;
    public int sequence;
    public Long sizeBytes;
    public LinkedHashMap<String, String> descriptions;

    public static Bitstream parseBitstreamFromJSON(JsonNode bitstreamNode) {
        Bitstream bitstream = new Bitstream();
        bitstream.name = bitstreamNode.get("name").asText();
        bitstream.description = bitstreamNode.get("description").asText();
        bitstream.descriptions = new LinkedHashMap<>();
        try {
            JsonNode descr = play.libs.Json.parse(bitstream.description);

            for (Iterator<String> de_iter = descr.fieldNames(); de_iter.hasNext(); ) {
                String label = de_iter.next();
                String value = "";
                if (descr.hasNonNull(label) && !descr.get(label).asText().equals("")) {
                    value = descr.get(label).asText();
                } else {
                    continue;
                }
                bitstream.descriptions.put(Messages.get(label), value);
                bitstream.description ="";
            }
        } catch (RuntimeException je) {
            bitstream.description = bitstreamNode.get("description").asText();
            Logger.info(je.getLocalizedMessage());
        }

        bitstream.format = bitstreamNode.get("format").asText();
        bitstream.mimeType = bitstreamNode.get("mimeType").asText();
        bitstream.bundleName = bitstreamNode.get("bundleName").asText();
        bitstream.sizeBytes = bitstreamNode.get("sizeBytes").asLong();
        bitstream.retrieveLink = bitstreamNode.get("retrieveLink").asText();
        bitstream.checksum = bitstreamNode.get("checkSum").get("value").asText();
        bitstream.sequence = bitstreamNode.get("sequenceId").asInt();
        return bitstream;
    }
}
