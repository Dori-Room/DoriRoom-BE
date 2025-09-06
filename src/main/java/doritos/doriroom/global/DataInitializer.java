package doritos.doriroom.global;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.AtlasReward;
import doritos.doriroom.atlas.repository.AtlasRepository;
import doritos.doriroom.atlas.repository.AtlasRewardRepository;
import doritos.doriroom.challenge.domain.challenge.*;
import doritos.doriroom.challenge.repository.ChallengeRepository;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.repository.EventRepository;
import doritos.doriroom.item.domain.CollectionTheme;
import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.repository.ItemRepository;
import doritos.doriroom.quiz.domain.Question;
import doritos.doriroom.quiz.domain.Quiz;
import doritos.doriroom.quiz.repository.QuizRepository;
import doritos.doriroom.tourApi.domain.Area;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.tourApi.repository.AreaRepository;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {
    private final AtlasRepository atlasRepository;
    private final ItemRepository itemRepository;
    private final QuizRepository quizRepository;
    private final ChallengeRepository challengeRepository;
    private final AtlasRewardRepository atlasRewardRepository;
    private final PasswordEncoder encoder;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AreaRepository areaRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 애플리케이션 시작 시 Atlas 데이터가 없으면 초기 데이터를 생성
        if (atlasRepository.count() == 0) {
            createInitialAtlases();
        }

        if(areaRepository.count() == 0){
            createInitialAreas();
        }

        if (itemRepository.count() == 0) {
            createInitialItems();
        }
        long itemCount = itemRepository.count();
        System.out.println("현재 아이템 개수: " + itemCount);

        if (challengeRepository.count() == 0) {
            createInitialCommonChallenges();
            createInitialQuizzesAndChallenges();
            createInitialFestivalChallenges();
            createInitialSidoVisitChallenges();
        }
        long challengeCount = challengeRepository.count();
        System.out.println("현재 도전과제 개수: " + challengeCount);

        // 도감 보상 데이터 생성 (Item과 Atlas가 먼저 생성된 후에 실행되어야 함)
        if (atlasRewardRepository.count() == 0) {
            createInitialAtlasRewards();
        }

        if (userRepository.count() == 0) {
            createInitialUsers();
        }

    }

    // 원본 지역 도감 데이터
    private void createInitialAtlases() {
        // AreaGroup Enum의 모든 값을 순회하며 Atlas 객체를 생성
        List<Atlas> initialAtlases = Arrays.stream(AreaGroup.values())
                .map(areaGroup -> Atlas.builder().areaGroup(areaGroup).build())
                .collect(Collectors.toList());

        atlasRepository.saveAll(initialAtlases);
        System.out.println(initialAtlases.size() + "개 지역 Atlas 초기 데이터가 생성되었습니다.");
    }

    // 지역 정보 데이터
    private void createInitialAreas() {
        List<Area> areas = List.of(
            Area.builder().code(1).name("서울").build(),
            Area.builder().code(2).name("인천").build(),
            Area.builder().code(3).name("대전").build(),
            Area.builder().code(4).name("대구").build(),
            Area.builder().code(5).name("광주").build(),
            Area.builder().code(6).name("부산").build(),
            Area.builder().code(7).name("울산").build(),
            Area.builder().code(8).name("세종").build(),
            Area.builder().code(31).name("경기").build(),
            Area.builder().code(32).name("강원").build(),
            Area.builder().code(33).name("충북").build(),
            Area.builder().code(34).name("충남").build(),
            Area.builder().code(35).name("경북").build(),
            Area.builder().code(36).name("경남").build(),
            Area.builder().code(37).name("전북").build(),
            Area.builder().code(38).name("전남").build(),
            Area.builder().code(39).name("제주").build()
        );

        areaRepository.saveAll(areas);
        System.out.println("지역 정보 초기화 완료: " + areas.size() + "개 지역");
    }

    // 아이템 초기 데이터
    private void createInitialItems() {
        List<Item> items = List.of(
                // --- SHELF ---
                Item.builder().name("사물함 선반").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.CLASSROOM).price(0L).isPurchasable(true).build(),
                Item.builder().name("굴뚝 선반").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.WINTER).price(10L).isPurchasable(true).build(),
                Item.builder().name("캠핑의자 선반").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.PICNIC).price(20L).isPurchasable(true).build(),
                Item.builder().name("파라솔 선반").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.SUMMER).price(30L).isPurchasable(true).build(),
                Item.builder().name("아이스크림 선반").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.DESSERT).price(40L).isPurchasable(true).build(),
                Item.builder().name("선물 선반").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.BIRTHDAY).price(50L).isPurchasable(true).build(),

                // --- OBJECT ---
                Item.builder().name("책").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.CLASSROOM).price(10L).isPurchasable(true).build(),
                Item.builder().name("이글루").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.WINTER).price(20L).isPurchasable(true).build(),
                Item.builder().name("사과바구니").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.PICNIC).price(30L).isPurchasable(true).build(),
                Item.builder().name("해바라기").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.SUMMER).price(40L).isPurchasable(true).build(),
                Item.builder().name("사탕다발").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.DESSERT).price(50L).isPurchasable(true).build(),
                Item.builder().name("풍선개").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.BIRTHDAY).price(60L).isPurchasable(true).build(),

                // --- WINDOW ---
                Item.builder().name("비행기 창문").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.CLASSROOM).price(0L).isPurchasable(true).build(),
                Item.builder().name("눈밭 창문").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.WINTER).price(10L).isPurchasable(true).build(),
                Item.builder().name("자연 창문").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.PICNIC).price(20L).isPurchasable(true).build(),
                Item.builder().name("야자수 창문").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.SUMMER).price(30L).isPurchasable(true).build(),
                Item.builder().name("초콜릿 창문").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.DESSERT).price(40L).isPurchasable(true).build(),
                Item.builder().name("전구 창문").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.BIRTHDAY).price(50L).isPurchasable(true).build(),

                // --- FLOOR ---
                Item.builder().name("교실 바닥").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.CLASSROOM).price(10L).isPurchasable(true).build(),
                Item.builder().name("얼음 바닥").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.WINTER).price(20L).isPurchasable(true).build(),
                Item.builder().name("식탁보 바닥").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.PICNIC).price(30L).isPurchasable(true).build(),
                Item.builder().name("모래사장 바닥").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.SUMMER).price(40L).isPurchasable(true).build(),
                Item.builder().name("쿠키 바닥").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.DESSERT).price(50L).isPurchasable(true).build(),
                Item.builder().name("핑크 카펫 바닥").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.BIRTHDAY).price(60L).isPurchasable(true).build(),

                // --- WALL ---
                Item.builder().name("칠판 벽지").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.CLASSROOM).price(10L).isPurchasable(true).build(),
                Item.builder().name("눈꽃 벽지").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.WINTER).price(20L).isPurchasable(true).build(),
                Item.builder().name("구름 벽지").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.PICNIC).price(30L).isPurchasable(true).build(),
                Item.builder().name("조개 벽지").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.SUMMER).price(40L).isPurchasable(true).build(),
                Item.builder().name("롤리팝 벽지").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.DESSERT).price(50L).isPurchasable(true).build(),
                Item.builder().name("풍선 벽지").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.BIRTHDAY).price(60L).isPurchasable(true).build(),

                // --- APPAREL ---
                Item.builder().name("도리").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(null).price(0L).isPurchasable(true).build(),
                Item.builder().name("학사모 도리").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.CLASSROOM).price(10L).isPurchasable(true).build(),
                Item.builder().name("목도리 도리").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.WINTER).price(20L).isPurchasable(true).build(),
                Item.builder().name("빨간망토 도리").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.PICNIC).price(30L).isPurchasable(true).build(),
                Item.builder().name("튜브 도리").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.SUMMER).price(40L).isPurchasable(true).build(),
                Item.builder().name("메론빵 도리").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.DESSERT).price(50L).isPurchasable(true).build(),
                Item.builder().name("생일파티 도리").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).areaGroup(null).theme(CollectionTheme.BIRTHDAY).price(60L).isPurchasable(true).build(),


                // --- 지역(AREA) 그룹 아이템 (임시 도감 보상용)
                Item.builder().name("서울 남산타워 모형").itemType(ItemType.OBJECT).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.SEOUL).price(0L).isPurchasable(false).build(),
                Item.builder().name("경기도 행궁 담벼락").itemType(ItemType.WALL).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.GYEONGGI).price(0L).isPurchasable(false).build(),
                Item.builder().name("강원도 오징어 인형").itemType(ItemType.APPAREL).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.GANGWON).price(0L).isPurchasable(false).build(),
                Item.builder().name("충청도 소나무 분재").itemType(ItemType.OBJECT).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.CHUNGNAM).price(0L).isPurchasable(false).build(),
                Item.builder().name("전라도 풍년 볏짚 바닥").itemType(ItemType.FLOOR).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.JEOLLA).price(0L).isPurchasable(false).build(),
                Item.builder().name("경상도 돌고래 창문").itemType(ItemType.WINDOW).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.GYEONGSANG).price(0L).isPurchasable(false).build(),
                Item.builder().name("제주 유채꽃 선반").itemType(ItemType.SHELF).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.JEJU).price(0L).isPurchasable(false).build()


        );

        itemRepository.saveAll(items);
        System.out.println(items.size() + "개 아이템이 생성되었습니다.");
    }

    // 일반과제 초기 데이터
    private void createInitialCommonChallenges() {
        // 1. 일기 작성
        Challenge writeDiary = createCommonChallenge("일기 5개 작성하기", "여행의 순간을 담은 일기를 5번 작성해보세요.",
                ChallengeType.WRITE_DIARY, 5);

        // 2. 이웃 집 방문
        Challenge visitNeighbor = createCommonChallenge("이웃 10회 방문하기", "다른 사용자의 방을 10번 방문해보세요.",
                ChallengeType.VISIT_NEIGHBOR, 10);

        // 3. 방 조회수 N번 달성
        Challenge reachVisitCount = createCommonChallenge("내 방 방문객 20명 달성하기", "내 방의 총 방문객 수를 20명 달성해보세요.",
                ChallengeType.REACH_VISIT_COUNT, 20);

        // 4. 이웃 N명 달성
        Challenge reachNeighborCount = createCommonChallenge("이웃 10명 달성하기", "총 10명의 이웃(팔로잉)을 만들어보세요.",
                ChallengeType.REACH_NEIGHBOR_COUNT, 10);

        // 5. 방 좋아요 수 N개 달성
        Challenge reachRoomLikeCount = createCommonChallenge("방 좋아요 15개 받기", "내 방의 좋아요 수를 15개 달성해보세요.",
                ChallengeType.REACH_ROOM_COUNT, 15);

        // 6. 아이템 수집
        Challenge collectItem = createCommonChallenge("아이템 3개 수집하기", "종류에 상관없이 아이템을 3개 수집해보세요.",
                ChallengeType.COLLECT_ITEM, 3);

        challengeRepository.saveAll(List.of(writeDiary, visitNeighbor, reachVisitCount, reachNeighborCount, reachRoomLikeCount, collectItem));
        System.out.println("일반 과제 초기 데이터가 6개 생성되었습니다.");
    }

    // 도전과제 초기 데이터 (지역 퀴즈)
    private void createInitialQuizzesAndChallenges() {
        List<Quiz> allQuizzes = new ArrayList<>();
        List<Challenge> allChallenges = new ArrayList<>();

        // --- 서울 퀴즈 (2개) ---
        Challenge seoulOxChallenge = createQuizChallenge("서울 O/X 퀴즈", AreaGroup.SEOUL);
        allChallenges.add(seoulOxChallenge);
        Quiz seoulOxQuiz = Quiz.builder().challenge(seoulOxChallenge).title("서울 O/X 퀴즈").build();
        seoulOxQuiz.getQuestions().addAll(List.of(
                createOxQuestion(seoulOxQuiz, 1, "서울은 조선 시대부터 수도 역할을 해왔다.", (byte)1, "1394년 태조 이성계가 한양으로 천도하며 조선의 수도가 되었습니다."),
                createOxQuestion(seoulOxQuiz, 2, "서울에는 경복궁, 창덕궁, 창경궁, 덕수궁이 모두 있다.", (byte)1, "서울에는 5대 궁궐(경복궁, 창덕궁, 창경궁, 덕수궁, 경희궁)이 모두 있습니다."),
                createOxQuestion(seoulOxQuiz, 3, "서울의 한강은 낙동강보다 길다.", (byte)2, "낙동강(약 510km)은 한강(약 494km)보다 깁니다."),
                createOxQuestion(seoulOxQuiz, 4, "서울은 광역시로 분류된다.", (byte)2, "서울은 광역시가 아닌 특별시로 지정되어 있습니다."),
                createOxQuestion(seoulOxQuiz, 5, "서울의 면적은 대한민국에서 가장 넓다.", (byte)2, "가장 면적이 넓은 도시는 안동시이며, 서울은 그보다 작습니다.")
        ));
        allQuizzes.add(seoulOxQuiz);

        Challenge seoulMcChallenge = createQuizChallenge("서울 4지선다 퀴즈", AreaGroup.SEOUL);
        allChallenges.add(seoulMcChallenge);
        Quiz seoulMcQuiz = Quiz.builder().challenge(seoulMcChallenge).title("서울 4지선다 퀴즈").build();
        seoulMcQuiz.getQuestions().addAll(List.of(
                createMcQuestion(seoulMcQuiz, 1, "서울의 대표적인 대학로 문화 공간은?", "동대문디자인플라자", "세종문화회관", "예술의전당", "마로니에공원", (byte)4, "마로니에공원은 대학로의 상징적인 문화예술 공간입니다."),
                createMcQuestion(seoulMcQuiz, 2, "다음 중 서울에 위치한 관광 명소가 아닌 것은?", "남산타워", "북촌한옥마을", "한라산", "광화문광장", (byte)3, "한라산은 제주도에 위치한 대한민국 최고봉입니다."),
                createMcQuestion(seoulMcQuiz, 3, "서울을 가로지르는 강은?", "낙동강", "금강", "한강", "섬진강", (byte)3, "한강은 서울을 가로지르는 대표적인 강입니다."),
                createMcQuestion(seoulMcQuiz, 4, "서울의 중심구역으로 청와대와 경복궁이 있는 자치구는?", "종로구", "마포구", "강남구", "성동구", (byte)1, "종로구는 서울의 역사, 정치의 핵심 지역입니다.")
        ));
        allQuizzes.add(seoulMcQuiz);

        // --- 경기도 퀴즈 (2개) ---
        Challenge gyeonggiOxChallenge = createQuizChallenge("경기도 O/X 퀴즈", AreaGroup.GYEONGGI);
        allChallenges.add(gyeonggiOxChallenge);
        Quiz gyeonggiOxQuiz = Quiz.builder().challenge(gyeonggiOxChallenge).title("경기도 O/X 퀴즈").build();
        gyeonggiOxQuiz.getQuestions().addAll(List.of(
                createOxQuestion(gyeonggiOxQuiz, 1, "경기도는 서울을 둘러싸고 있는 지역이다.", (byte)1, "경기도는 서울을 사방으로 둘러싸고 있습니다."),
                createOxQuestion(gyeonggiOxQuiz, 2, "수원은 경기도의 도청 소재지다.", (byte)1, "경기도청은 현재 수원시에 위치해 있습니다."),
                createOxQuestion(gyeonggiOxQuiz, 3, "경기도는 바다와 접해 있지 않다.", (byte)2, "경기도는 서해안과 접해 있으며 평택, 화성 등의 해안 도시가 있습니다."),
                createOxQuestion(gyeonggiOxQuiz, 4, "판문점은 경기도에 위치해 있다.", (byte)1, "판문점은 경기도 파주시에 위치한 공동경비구역(JSA)입니다."),
                createOxQuestion(gyeonggiOxQuiz, 5, "고양시는 강원도에 속해 있다.", (byte)2, "고양시는 경기도 북서부에 위치한 도시입니다.")
        ));
        allQuizzes.add(gyeonggiOxQuiz);

        Challenge gyeonggiMcChallenge = createQuizChallenge("경기도 4지선다 퀴즈", AreaGroup.GYEONGGI);
        allChallenges.add(gyeonggiMcChallenge);
        Quiz gyeonggiMcQuiz = Quiz.builder().challenge(gyeonggiMcChallenge).title("경기도 4지선다 퀴즈").build();
        gyeonggiMcQuiz.getQuestions().addAll(List.of(
                createMcQuestion(gyeonggiMcQuiz, 1, "다음 중 경기도에 속한 도시는?", "대전", "수원", "전주", "창원", (byte)2, "수원은 경기도의 대표 도시이며, 경기도 남부의 중심 도시입니다."),
                createMcQuestion(gyeonggiMcQuiz, 2, "다음 중 경기도에 있는 세계문화유산은?", "불국사", "해인사", "수원 화성", "공산성", (byte)3, "수원 화성은 1997년 유네스코 세계문화유산으로 지정되었습니다."),
                createMcQuestion(gyeonggiMcQuiz, 3, "다음 중 경기도 북부에 위치한 도시는?", "이천", "성남", "파주", "평택", (byte)3, "파주시는 경기도 북서부에 위치한 접경 도시입니다."),
                createMcQuestion(gyeonggiMcQuiz, 4, "경기도의 전통시장 중 ‘전통 한과’로 유명한 곳은?", "모란시장", "남대문시장", "이천시장", "정선시장", (byte)1, "성남 모란시장은 다양한 물품과 함께 전통 한과로도 매우 유명합니다.")
        ));
        allQuizzes.add(gyeonggiMcQuiz);

        // --- 충청도 퀴즈 (2개) ---
        Challenge chungcheongOxChallenge = createQuizChallenge("충청도 O/X 퀴즈", AreaGroup.CHUNGNAM);
        allChallenges.add(chungcheongOxChallenge);
        Quiz chungcheongOxQuiz = Quiz.builder().challenge(chungcheongOxChallenge).title("충청도 O/X 퀴즈").build();
        chungcheongOxQuiz.getQuestions().addAll(List.of(
                createOxQuestion(chungcheongOxQuiz, 1, "충청도는 행정구역상 충북과 충남으로 나뉜다.", (byte)1, "현재 충청북도와 충청남도로 행정구역이 나뉘어 있습니다."),
                createOxQuestion(chungcheongOxQuiz, 2, "세종특별자치시는 원래 충청남도의 일부였다.", (byte)1, "세종시는 2012년 충청남도 연기군을 중심으로 신설된 특별자치시입니다."),
                createOxQuestion(chungcheongOxQuiz, 3, "대전은 충청북도의 도청 소재지다.", (byte)2, "충청북도의 도청 소재지는 청주입니다."),
                createOxQuestion(chungcheongOxQuiz, 4, "충청도는 내륙 지방이기 때문에 바다가 없다.", (byte)2, "충청남도는 서해안에 접해 있습니다."),
                createOxQuestion(chungcheongOxQuiz, 5, "충청북도에는 속리산이 있다.", (byte)1, "속리산은 충북 보은군과 경북 상주에 걸쳐 있는 산입니다.")
        ));
        allQuizzes.add(chungcheongOxQuiz);

        Challenge chungcheongMcChallenge = createQuizChallenge("충청도 4지선다 퀴즈", AreaGroup.CHUNGNAM);
        allChallenges.add(chungcheongMcChallenge);
        Quiz chungcheongMcQuiz = Quiz.builder().challenge(chungcheongMcChallenge).title("충청도 4지선다 퀴즈").build();
        chungcheongMcQuiz.getQuestions().addAll(List.of(
                createMcQuestion(chungcheongMcQuiz, 1, "다음 중 충청남도에 속한 도시는?", "청주", "천안", "상주", "구미", (byte)2, "천안시는 충청남도의 북부에 위치한 도시입니다."),
                createMcQuestion(chungcheongMcQuiz, 2, "충북의 대표 산업 도시로서 오송 바이오밸리가 위치한 곳은?", "충주", "청주", "제천", "옥천", (byte)2, "오송 바이오밸리는 청주시 오송읍에 위치합니다."),
                createMcQuestion(chungcheongMcQuiz, 3, "세종특별자치시는 어느 기능을 분담하고자 설치되었나?", "교육", "행정", "국방", "문화", (byte)2, "세종시는 행정중심복합도시로 기능하고 있습니다."),
                createMcQuestion(chungcheongMcQuiz, 4, "충청도의 대표적인 향토 음식은?", "해물파전", "순대국밥", "올갱이국", "비빔밥", (byte)3, "올갱이국(다슬기국)은 충청북도의 대표적인 향토 음식입니다.")
        ));
        allQuizzes.add(chungcheongMcQuiz);

        // --- 강원도 퀴즈 (2개) ---
        Challenge gangwonOxChallenge = createQuizChallenge("강원도 O/X 퀴즈", AreaGroup.GANGWON);
        allChallenges.add(gangwonOxChallenge);
        Quiz gangwonOxQuiz = Quiz.builder().challenge(gangwonOxChallenge).title("강원도 O/X 퀴즈").build();
        gangwonOxQuiz.getQuestions().addAll(List.of(
                createOxQuestion(gangwonOxQuiz, 1, "강원도는 동해와 접해 있다.", (byte)1, "강원도는 동쪽으로 동해와 접해 있습니다."),
                createOxQuestion(gangwonOxQuiz, 2, "평창은 동계올림픽 개최지였다.", (byte)1, "2018년 평창 동계올림픽이 개최되었습니다."),
                createOxQuestion(gangwonOxQuiz, 3, "강릉은 강원도의 도청 소재지다.", (byte)2, "강원도의 도청 소재지는 춘천입니다."),
                createOxQuestion(gangwonOxQuiz, 4, "설악산은 강원도에 위치한다.", (byte)1, "설악산은 강원도 속초시, 인제군, 양양군 등에 걸쳐 있습니다."),
                createOxQuestion(gangwonOxQuiz, 5, "태백은 충청도에 위치한 산이다.", (byte)2, "태백산은 강원도와 경상북도 경계에 위치한 산입니다.")
        ));
        allQuizzes.add(gangwonOxQuiz);

        Challenge gangwonMcChallenge = createQuizChallenge("강원도 4지선다 퀴즈", AreaGroup.GANGWON);
        allChallenges.add(gangwonMcChallenge);
        Quiz gangwonMcQuiz = Quiz.builder().challenge(gangwonMcChallenge).title("강원도 4지선다 퀴즈").build();
        gangwonMcQuiz.getQuestions().addAll(List.of(
                createMcQuestion(gangwonMcQuiz, 1, "다음 중 강원도에 위치한 도시는?", "순천", "태백", "군산", "상주", (byte)2, "태백시는 강원도 남동부에 위치한 고산지대 도시입니다."),
                createMcQuestion(gangwonMcQuiz, 2, "2018 평창 동계올림픽에서 사용된 슬로건은?", "Passion. Connected.", "Be the One", "Peace and Harmony", "Go Together", (byte)1, "이 슬로건은 ‘열정이 하나로 연결된다’는 의미입니다."),
                createMcQuestion(gangwonMcQuiz, 3, "강원도의 대표 전통 음식으로, 감자와 관련된 음식은?", "감자탕", "감자전", "감자떡", "감자고로케", (byte)3, "강원도는 감자를 이용한 감자떡이 향토 음식으로 유명합니다."),
                createMcQuestion(gangwonMcQuiz, 4, "다음 중 강원도에 위치한 관광지는?", "한라산", "해운대", "정동진", "대둔산", (byte)3, "정동진은 강릉에 위치한 유명한 해안 관광지입니다.")
        ));
        allQuizzes.add(gangwonMcQuiz);

        // --- 제주도 퀴즈 (1개) ---
        Challenge jejuChallenge = createQuizChallenge("제주도 종합 퀴즈", AreaGroup.JEJU);
        allChallenges.add(jejuChallenge);
        Quiz jejuQuiz = Quiz.builder().challenge(jejuChallenge).title("제주도 종합 퀴즈").build();
        jejuQuiz.getQuestions().addAll(List.of(
                // 1~3번: O/X 문제
                createOxQuestion(jejuQuiz, 1, "제주도는 대한민국에서 두번째로 큰 섬이다.", (byte)2, "제주도는 남한에서 가장 큰 섬입니다."),
                createOxQuestion(jejuQuiz, 2, "제주도는 화산 활동으로 생긴 섬이다.", (byte)1, "제주도는 약 200만 년 전부터 일어난 화산 활동으로 형성된 화산섬입니다."),
                createOxQuestion(jejuQuiz, 3, "제주도는 우리나라에서 유일하게 특별자치도로 지정된 지역이다.", (byte)1, "2006년에 제주도는 '제주특별자치도'로 지정되었습니다."),

                // 4~5번: 4지선다 문제
                createMcQuestion(jejuQuiz, 4, "다음 중 제주도에서 흔히 볼 수 있는 전통 조형물은?", "돌하르방", "석굴암", "첨성대", "팔각정", (byte)1, "돌하르방은 제주도의 상징적인 석상으로, 마을 입구에 세워 수호신 역할을 해왔어요."),
                createMcQuestion(jejuQuiz, 5, "다음 중 ‘제주도의 삼다(三多)’에 해당하지 않는 것은?", "바람", "돌", "여자", "귤", (byte)4, "제주의 삼다(三多)는 바람, 돌, 여자 세 가지입니다.")
        ));
        allQuizzes.add(jejuQuiz);

        // --- 전라도 퀴즈 (2개) ---
        Challenge jeollaOxChallenge = createQuizChallenge("전라도 O/X 퀴즈", AreaGroup.JEOLLA);
        allChallenges.add(jeollaOxChallenge);
        Quiz jeollaOxQuiz = Quiz.builder().challenge(jeollaOxChallenge).title("전라도 O/X 퀴즈").build();
        jeollaOxQuiz.getQuestions().addAll(List.of(
                createOxQuestion(jeollaOxQuiz, 1, "광주광역시는 전라남도에 속해 있다.", (byte)2, "광주광역시는 전라남도 소속이 아닌 독립된 광역시입니다."),
                createOxQuestion(jeollaOxQuiz, 2, "전라도는 전북과 전남으로 나뉜다.", (byte)1, "전라북도는 서해안과 접한 대한민국 서남부에 위치한 도입니다."),
                createOxQuestion(jeollaOxQuiz, 3, "전주 비빔밥은 전라북도를 대표하는 음식이다.", (byte)1, "전주 한옥마을은 전라북도의 대표 관광지입니다."),
                createOxQuestion(jeollaOxQuiz, 4, "여수는 전라남도에 속해 있다.", (byte)1, "여수시는 전라남도에 속해 있으며, 대표적인 남해안 항구 도시입니다."),
                createOxQuestion(jeollaOxQuiz, 5, "목포는 내륙 도시이다.", (byte)2, "해남은 전라남도 최남단에 위치한 도시입니다.")
        ));
        allQuizzes.add(jeollaOxQuiz);

        Challenge jeollaMcChallenge = createQuizChallenge("전라도 4지선다 퀴즈", AreaGroup.JEOLLA);
        allChallenges.add(jeollaMcChallenge);
        Quiz jeollaMcQuiz = Quiz.builder().challenge(jeollaMcChallenge).title("전라도 4지선다 퀴즈").build();
        jeollaMcQuiz.getQuestions().addAll(List.of(
                createMcQuestion(jeollaMcQuiz, 1, "다음 중 전라남도에 있는 도시는?", "익산", "순천", "군산", "청주", (byte)2, "순천은 전라남도에 위치한 생태 도시입니다."),
                createMcQuestion(jeollaMcQuiz, 2, "전라북도 전주의 대표적인 전통 문화 공간은?", "경복궁", "한옥마을", "전주성", "남이섬", (byte)2, "전주 한옥마을은 전라북도의 대표적인 전통 문화 공간입니다."),
                createMcQuestion(jeollaMcQuiz, 3, "광주는 어떤 산업으로 유명한 도시인가?", "조선업", "반도체", "자동차", "문화예술", (byte)4, "광주는 아시아문화중심도시로 문화예술 산업이 발달했습니다."),
                createMcQuestion(jeollaMcQuiz, 4, "전라도 지역에서 볼 수 있는 민속놀이로 적절한 것은?", "줄다리기", "씨름", "강강술래", "말타기", (byte)3, "강강술래는 주로 남해안 지역에서 전승되는 대표적인 민속놀이입니다.")
        ));
        allQuizzes.add(jeollaMcQuiz);

        // --- 경상도 퀴즈 (2개) ---
        Challenge gyeongsangOxChallenge = createQuizChallenge("경상도 O/X 퀴즈", AreaGroup.GYEONGSANG);
        allChallenges.add(gyeongsangOxChallenge);
        Quiz gyeongsangOxQuiz = Quiz.builder().challenge(gyeongsangOxChallenge).title("경상도 O/X 퀴즈").build();
        gyeongsangOxQuiz.getQuestions().addAll(List.of(
                createOxQuestion(gyeongsangOxQuiz, 1, "경주의 불국사는 유네스코 세계문화유산에 등재되어 있다.", (byte)1, "불국사와 석굴암은 1995년 유네스코 세계문화유산에 함께 등재되었습니다."),
                createOxQuestion(gyeongsangOxQuiz, 2, "하회마을은 경상북도 안동시에 위치해 있다.", (byte)1, "하회마을은 풍산 류씨 집성촌으로 유네스코 세계유산으로 등재되어 있습니다."),
                createOxQuestion(gyeongsangOxQuiz, 3, "청도는 매년 세계적인 벚꽃 축제를 개최하는 도시로 유명하다.", (byte)2, "벚꽃 축제로 유명한 도시는 진해(창원)이며, 청도는 소싸움 축제로 유명합니다."),
                createOxQuestion(gyeongsangOxQuiz, 4, "울산의 대왕암공원은 동해의 해돋이를 볼 수 있는 명소 중 하나다.", (byte)1, "대왕암공원은 울산 동구에 위치한 해안 절경지로, 일출 명소로 유명합니다.")
        ));
        allQuizzes.add(gyeongsangOxQuiz);

        Challenge gyeongsangMcChallenge = createQuizChallenge("경상도 4지선다 퀴즈", AreaGroup.GYEONGSANG);
        allChallenges.add(gyeongsangMcChallenge);
        Quiz gyeongsangMcQuiz = Quiz.builder().challenge(gyeongsangMcChallenge).title("경상도 4지선다 퀴즈").build();
        gyeongsangMcQuiz.getQuestions().addAll(List.of(
                createMcQuestion(gyeongsangMcQuiz, 1, "경상북도 경주에서 볼 수 있는 유적이 아닌 것은?", "첨성대", "석굴암", "대릉원", "김해 가야테마파크", (byte)4, "김해 가야테마파크는 경상남도 김해시에 위치해 있습니다."),
                createMcQuestion(gyeongsangMcQuiz, 2, "다음 중 안동에서 즐길 수 있는 문화 체험으로 적절한 것은?", "판소리 체험", "하회탈 만들기", "해녀 체험", "대나무 공예 체험", (byte)2, "하회탈은 안동 하회마을의 전통 가면극에 사용되는 탈입니다."),
                createMcQuestion(gyeongsangMcQuiz, 3, "진주 유등축제는 어떤 장소에서 주로 열린다?", "남강", "낙동강", "섬진강", "한강", (byte)1, "진주 유등축제는 진주 남강을 중심으로 펼쳐지는 대표적인 가을 축제입니다."),
                createMcQuestion(gyeongsangMcQuiz, 4, "다음 중 경상도 지역의 섬이 아닌 것은?", "울릉도", "욕지도", "거제도", "백령도", (byte)4, "백령도는 인천광역시에 속한 서해안의 섬입니다.")
        ));
        allQuizzes.add(gyeongsangMcQuiz);

        // 모든 퀴즈 세트를 한 번에 저장
        challengeRepository.saveAll(allChallenges);
        quizRepository.saveAll(allQuizzes);
        System.out.println("총 " + allQuizzes.size() + "개의 퀴즈 세트 초기 데이터가 생성되었습니다.");
    }

    // 지역 축제 관련 과제 추가
    private void createInitialFestivalChallenges() {
        List<Challenge> challenges = new ArrayList<>();

        // --- 축제 과제 목록 ---
        challenges.add(createFestivalChallenge("보령머드축제 방문하기", AreaGroup.CHUNGNAM, 506534));
        challenges.add(createFestivalChallenge("춘천막국수닭갈비축제 방문하기", AreaGroup.GANGWON, 1230074));
        challenges.add(createFestivalChallenge("APAP 작품투어 참여하기", AreaGroup.GYEONGGI, 3113265));
        challenges.add(createFestivalChallenge("DDP 건축투어 참여하기", AreaGroup.SEOUL, 3473295));
        challenges.add(createFestivalChallenge("광안리 M 드론라이트 쇼 보기", AreaGroup.GYEONGSANG, 2786391));
        challenges.add(createFestivalChallenge("목포해상W쇼 보기", AreaGroup.JEOLLA, 2774275));
        challenges.add(createFestivalChallenge("휴애리 유럽 수국축제 방문하기", AreaGroup.JEJU, 2817255));

        // Event ID 조회 실패로 null이 포함된 경우 제거
        challenges.removeIf(Objects::isNull);

        challengeRepository.saveAll(challenges);
        System.out.println(challenges.size() + "개의 축제 방문 과제 초기 데이터가 생성되었습니다.");
    }

    // 지역 도감 보상 아이템 추가
    private void createInitialAtlasRewards() {
        // 각 지역과 보상으로 지급할 아이템 ID
        Map<AreaGroup, Long> rewardItemMap = Map.of(
                AreaGroup.SEOUL, 38L,
                AreaGroup.GYEONGGI, 39L,
                AreaGroup.GANGWON, 40L,
                AreaGroup.CHUNGNAM, 41L,
                AreaGroup.JEOLLA, 42L,
                AreaGroup.GYEONGSANG, 43L,
                AreaGroup.JEJU, 44L
        );

        List<AtlasReward> rewards = new ArrayList<>();

        rewardItemMap.forEach((areaGroup, itemId) -> {

            Atlas atlas = atlasRepository.findByAreaGroup(areaGroup).orElse(null);
            Item rewardItem = itemRepository.findById(itemId).orElse(null);


            if (atlas != null && rewardItem != null) {
                rewards.add(AtlasReward.builder()
                        .atlas(atlas)
                        .targetLevel(1) // 목표 레벨
                        .rewardItem(rewardItem)
                        .build());
            }
        });

        atlasRewardRepository.saveAll(rewards);
        System.out.println(rewards.size() + "개의 도감 보상 초기 데이터가 생성되었습니다.");
    }

    // 임의의 유저 추가
    private void createInitialUsers() {
        List<User> users = new ArrayList<>();
        String encodedPassword = encoder.encode("Passw0rd!"); // 모든 유저의 기본 비밀번호

        for (int i = 1; i <= 20; i++) {
            User user = User.builder()
                    .userId(UUID.randomUUID()) // UUID 직접 생성
                    .username("user" + String.format("%02d", i)) // user01, user02 ...
                    .password(encodedPassword)
                    .email("user" + String.format("%02d", i) + "@dori.com") // user01@dori.com ...
                    .nickname("도리" + i) // 도리1, 도리2 ...
                    // credit, roomVisibility, likeCount, viewCount 등은 @Builder.Default로 자동 설정됩니다.
                    .build();
            users.add(user);
        }

        userRepository.saveAll(users);
        System.out.println("유저 " + users.size() + "명이 생성되었습니다.");
    }

    //시도 방문 과제 초기 데이터 생성
    private void createInitialSidoVisitChallenges() {
        List<Challenge> challenges = new ArrayList<>();

        // 각 AreaGroup별로 시도 방문 과제 생성
        for (AreaGroup areaGroup : AreaGroup.values()) {
            Challenge challenge = createSidoVisitChallenge(areaGroup);
            challenges.add(challenge);
        }

        challengeRepository.saveAll(challenges);
        log.info("{}개의 시도 방문 과제 초기 데이터가 생성되었습니다.", challenges.size());
    }




    // --- Helper Methods ---

    // 지역 축제 과제
    private Challenge createFestivalChallenge(String title, AreaGroup areaGroup, int contentId) {
        Event event = eventRepository.findByContentId(contentId).orElse(null);
        if (event == null) {
            System.out.println("WARN: Content ID " + contentId + "를 찾을 수 없어 과제를 생성하지 못했습니다.");
            return null;
        }

        log.info("축제 좌표 생성: {}, contentId: {}", title, event.getContentId());
        String polygon = getEventPolygonFromFile(event.getContentId());
        log.info("축제 polygon 결과: {}", polygon != null ? "성공" : "실패");

        Challenge challenge = Challenge.builder()
                .title(title)
                .challengeGroup(ChallengeGroup.AREA)
                .challengeType(ChallengeType.VISIT_EVENT)
                .targetCount(1)
                .areaGroup(areaGroup)
                .event(event)
                .polygon(polygon)
                .build();

        // 보상: 크레딧 20 + 도감 경험치 200
        challenge.getRewards().add(ChallengeReward.builder().challenge(challenge).rewardType(RewardType.CREDIT).amount(20L).build());
        challenge.getRewards().add(ChallengeReward.builder().challenge(challenge).rewardType(RewardType.EXP).amount(200L).build());

        return challenge;
    }


//    private void createItem(String name, ItemType type, ItemGroup group, Long price, boolean isPurchasable, AreaGroup areaGroup, CollectionTheme theme) {
////        // 이름으로 아이템이 이미 존재하는지 확인
////        if (!itemRepository.existsByName(name)) {
////            // 존재하지 않을 때만 Item 객체를 생성하여 저장
//        Item item = Item.builder()
//                .name(name)
//                .itemType(type)
//                .itemGroup(group)
//                .price(price)
//                .isPurchasable(isPurchasable)
//                .areaGroup(areaGroup)
//                .theme(theme)
//                .build();
//        itemRepository.save(item);
    ////        }
//    }

    // 일반 과제 생성 헬퍼
    private Challenge createCommonChallenge(String title, String content, ChallengeType type, int targetCount) {
        Challenge challenge = Challenge.builder()
                .title(title)
                .content(content)
                .challengeGroup(ChallengeGroup.COMMON)
                .challengeType(type)
                .targetCount(targetCount)
                .build();
        // 보상: 크레딧 4
        challenge.getRewards().add(ChallengeReward.builder().challenge(challenge).rewardType(RewardType.CREDIT).amount(4L).build());
        return challenge;
    }

    // 지역 과제(퀴즈) 생성 헬퍼
    private Challenge createQuizChallenge(String title, AreaGroup areaGroup) {
        Challenge challenge = Challenge.builder()
                .title(title)
                .challengeGroup(ChallengeGroup.AREA)
                .challengeType(ChallengeType.REGIONAL_QUIZ)
                .targetCount(1)
                .areaGroup(areaGroup)
                .startDate(null)
                .endDate(null)
                .build();
        // 보상: 크레딧 10 + 도감 경험치 150
        challenge.getRewards().add(ChallengeReward.builder().challenge(challenge).rewardType(RewardType.CREDIT).amount(10L).build());
        challenge.getRewards().add(ChallengeReward.builder().challenge(challenge).rewardType(RewardType.EXP).amount(150L).build());
        return challenge;
    }

    private Question createOxQuestion(Quiz quiz, int sequence, String content, byte correctAnswer, String commentary) {
        return Question.builder()
                .quiz(quiz)
                .sequence(sequence)
                .content(content)
                .option1("O") // O/X 퀴즈의 선택지는 String으로 고정
                .option2("X")
                .correctAnswer(correctAnswer)
                .commentary(commentary)
                .build();
    }

    private Question createMcQuestion(Quiz quiz, int sequence, String content, String opt1, String opt2, String opt3, String opt4, byte correctAnswer, String commentary) {
        return Question.builder()
                .quiz(quiz)
                .sequence(sequence)
                .content(content)
                .option1(opt1)
                .option2(opt2)
                .option3(opt3)
                .option4(opt4)
                .correctAnswer(correctAnswer)
                .commentary(commentary)
                .build();
    }

    private Challenge createSidoVisitChallenge(AreaGroup areaGroup) {
        String polygon = getAreaPolygonFromFile(areaGroup);

        Challenge challenge = Challenge.builder()
            .title(areaGroup.getName() + " 방문하기")
            .content(areaGroup.getName() + " 지역을 방문하여 인증하세요.")
            .challengeGroup(ChallengeGroup.AREA)
            .challengeType(ChallengeType.VISIT_EVENT)
            .targetCount(1)
            .areaGroup(areaGroup)
            .polygon(polygon)
            .startDate(null)
            .endDate(null)
            .build();

        // 보상: 크레딧 20 + 도감 경험치 200
        challenge.getRewards().add(ChallengeReward.builder().challenge(challenge).rewardType(RewardType.CREDIT).amount(20L).build());
        challenge.getRewards().add(ChallengeReward.builder().challenge(challenge).rewardType(RewardType.EXP).amount(200L).build());

        return challenge;
    }

    // 지역 polygon 좌표를 파일에서 가져오는 메서드
    private String getAreaPolygonFromFile(AreaGroup areaGroup) {
        Map<AreaGroup, String> polygonFile = Map.of(
            AreaGroup.SEOUL, "data/area-polygon/seoul.json",
            AreaGroup.GYEONGGI, "data/area-polygon/gyeonggi.json",
            AreaGroup.GANGWON, "data/area-polygon/gangwon.json",
            AreaGroup.GYEONGSANG, "data/area-polygon/gyeongsang.json",
            AreaGroup.JEOLLA, "data/area-polygon/jeolla.json",
            AreaGroup.CHUNGNAM, "data/area-polygon/chungnam.json",
            AreaGroup.JEJU, "data/area-polygon/jeju.json"
        );

        String filePath = polygonFile.get(areaGroup);
        if (filePath == null) {
            log.warn("지역 {}에 대한 polygon 파일이 설정되지 않았습니다.", areaGroup);
            return null;
        }

        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                log.warn("지역 좌표 파일이 존재하지 않습니다: {}", filePath);
                return null;
            }
            JsonNode coordinateData = objectMapper.readTree(resource.getInputStream());
            return objectMapper.writeValueAsString(coordinateData);
        } catch (Exception e) {
            log.error("지역 좌표 파일 로드 실패: {}", filePath, e);
            return null;
        }
    }

    //축제 polygon을 파일에서 가져오는 메서드
    private String getEventPolygonFromFile(int contentId) {
        String filePath = getString(contentId);
        if (filePath == null) {
            log.warn("축제 {}에 대한 polygon 파일이 설정되지 않았습니다.", contentId);
            return null;
        }

        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            if (!resource.exists()) {
                log.warn("축제 polygon 파일이 존재하지 않습니다: {}", filePath);
                return null;
            }
            JsonNode coordinateData = objectMapper.readTree(resource.getInputStream());
            return objectMapper.writeValueAsString(coordinateData);
        } catch (Exception e) {
            log.error("축제 polygon 파일 로드 실패: {}", filePath, e);
            return null;
        }
    }

    private static String getString(int contentId) {
        Map<Integer, String> eventPolygonFiles = Map.of(
            // contentId와 JSON 파일 경로 매핑
            3473295, "data/event-polygon/event_3473295.json",
            506534, "data/event-polygon/event_506534.json",
            1230074, "data/event-polygon/event_1230074.json",
            3113265, "data/event-polygon/event_3113265.json",
            2786391, "data/event-polygon/event_2786391.json",
            2774275, "data/event-polygon/event_2774275.json",
            2817255, "data/event-polygon/event_2817255.json"
        );

        String filePath = eventPolygonFiles.get(contentId);
        return filePath;
    }
}