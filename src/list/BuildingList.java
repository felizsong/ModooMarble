package list;

import javax.swing.*;
import java.util.HashMap;

public class BuildingList {
    // 위치별 건물 상태 관리
    private static HashMap<Integer, BuildingState> buildingStates = new HashMap<>();

    // 특정 위치의 건물 상태 가져오기
    public static BuildingState getBuildingState(int position) {
        return buildingStates.getOrDefault(position, new BuildingState());
    }

    // 특정 위치에 건물 상태 설정
    public static void setBuildingState(int position, BuildingState state) {
        buildingStates.put(position, state);
    }

    // 건물 상태 클래스 (빌라, 빌딩, 호텔 여부 저장)
    public static class BuildingState {
        public boolean hasVilla = false;
        public boolean hasBuilding = false;
        public boolean hasHotel = false;

        public BuildingState() {}

        public void setVilla() {
            this.hasVilla = true;
        }

        public void setBuilding() {
            this.hasBuilding = true;
        }

        public void setHotel() {
            this.hasHotel = true;
        }
    }
}
