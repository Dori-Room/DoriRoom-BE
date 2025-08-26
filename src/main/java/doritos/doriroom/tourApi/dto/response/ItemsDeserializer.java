package doritos.doriroom.tourApi.dto.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import doritos.doriroom.tourApi.dto.response.TourApiResponseDto.Items;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemsDeserializer extends JsonDeserializer<Items> {
    @Override
    public Items deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);

        Items items = new Items();

        //null인 경우
        if(node == null || node.isNull()) {
            items.setItem(new ArrayList<>());
            return items;
        }

        //빈 문자열인 경우
        if(node.isTextual() && node.asText().trim().isEmpty()) {
            items.setItem(new ArrayList<>());
            return items;
        }

        //item 필드가 없는 경우
        if(!node.has("item")){
            items.setItem(new ArrayList<>());
            return items;
        }

        JsonNode itemNode = node.get("item");

        // item이 null이거나 빈 문자열인 경우
        if (itemNode == null || itemNode.isNull() ||
            (itemNode.isTextual() && itemNode.asText().trim().isEmpty())) {
            items.setItem(new ArrayList<>());
            return items;
        }

        // item이 배열인 경우
        if (itemNode.isArray()) {
            List<TourApiItemDto> itemList = new ArrayList<>();
            for (JsonNode element : itemNode) {
                try {
                    TourApiItemDto item = jp.getCodec().treeToValue(element, TourApiItemDto.class);
                    if (item != null) {
                        itemList.add(item);
                    }
                } catch (Exception e) {
                    // 개별 아이템 파싱 실패 시 로그만 남기고 계속 진행
                    System.err.println("아이템 파싱 실패: " + e.getMessage());
                }
            }
            items.setItem(itemList);
            return items;
        }

        // item이 단일 객체인 경우
        if (itemNode.isObject()) {
            try {
                TourApiItemDto item = jp.getCodec().treeToValue(itemNode, TourApiItemDto.class);
                List<TourApiItemDto> itemList = new ArrayList<>();
                if (item != null) {
                    itemList.add(item);
                }
                items.setItem(itemList);
                return items;
            } catch (Exception e) {
                System.err.println("단일 아이템 파싱 실패: " + e.getMessage());
                items.setItem(new ArrayList<>());
                return items;
            }
        }

        // 기타 경우 빈 리스트 반환
        items.setItem(new ArrayList<>());
        return items;
    }

}
