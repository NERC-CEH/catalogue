package uk.ac.ceh.gateway.catalogue.converters.xml2GeminiDocument;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeListConverter {

    public static List<String> getListOfStrings(NodeList nodeList) {
        ArrayList<String> toReturn = new ArrayList<>();
        for(int i=0; i<nodeList.getLength(); i++){
            Node item = nodeList.item(i);
            if (item.getFirstChild() != null) {
                toReturn.add(item.getFirstChild().getNodeValue());
            }
        }
        return toReturn;
    }
}
