public class QuestionConfig {
    public String questionText;  // 문제 제목
    public String passage;       // 본문 내용
    public String[] choices;     // 선택지 배열
    public char[] keys;          // 선택지 키 (A,B,C...)
    public boolean hasImage;     // 이미지 유무
    public String imagePath;     // 이미지 경로
    public char answer;          // 정답 키 추가

    public QuestionConfig(String questionText, String passage, String[] choices, char[] keys, boolean hasImage, String imagePath, char answer) {
        this.questionText = questionText;
        this.passage = passage;
        this.choices = choices;
        this.keys = keys;
        this.hasImage = hasImage;
        this.imagePath = imagePath;
        this.answer = answer;
    }
}
