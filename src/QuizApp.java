import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// 퀴즈 앱 메인 클래스
public class QuizApp {
    JFrame frame;                    // 메인 윈도우 프레임 (전체 창)
    JPanel mainPanel;                // 문제들을 카드처럼 넘기는 메인 패널 (문제 화면)
    CardLayout cardLayout;           // 문제 전환용 카드 레이아웃 (문제 이동할 때 씀)
    JPanel navPanel;                 // 문제 목록 + 답 표시 패널 (오른쪽 사이드바)
    JButton[] navButtons;            // 문제 번호 버튼 배열 (Q1, Q2, ...)
    JLabel[] answerLabels;           // 각 문제별로 사용자가 고른 답 보여주는 라벨 배열

    static char[] userAnswers;       // 사용자가 고른 답 저장 배열 (문제 수 크기)
    private boolean submitted = false;      // 답 제출 여부 체크 (제출 완료하면 true)
    private List<Question> questions = new ArrayList<>(); // Question 객체 리스트 (문제 UI, 기능)

    private JButton submitButton;   // 답안 제출 버튼

    // 생성자 - 앱 초기화, UI 구성
    public QuizApp() {
        // 문제 데이터 가져오기 (QuestionData에 저장된 문제들)
        QuestionConfig[] configs = QuestionData.QUESTIONS;
        userAnswers = new char[configs.length]; // 문제 수만큼 답 저장 공간 생성 (초기값 '\u0000')

        // JFrame 기본 세팅
        frame = new JFrame("세계사 모의고사");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setLayout(new BorderLayout());  // 동서남북 중앙 배치 가능

        cardLayout = new CardLayout();          // 카드 레이아웃 생성
        mainPanel = new JPanel(cardLayout);     // 문제 화면용 패널에 카드 레이아웃 적용

        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.X_AXIS)); // 왼쪽, 오른쪽 컬럼 나란히 배치

        // 왼쪽, 오른쪽 문제 버튼 컬럼 패널
        JPanel leftColumn = new JPanel();
        leftColumn.setLayout(new BoxLayout(leftColumn, BoxLayout.Y_AXIS));
        leftColumn.setAlignmentY(Component.TOP_ALIGNMENT);

        JPanel rightColumn = new JPanel();
        rightColumn.setLayout(new BoxLayout(rightColumn, BoxLayout.Y_AXIS));
        rightColumn.setAlignmentY(Component.TOP_ALIGNMENT);

        navButtons = new JButton[configs.length];
        answerLabels = new JLabel[configs.length];

        // 문제 수만큼 UI 컴포넌트 생성 반복
        for (int i = 0; i < configs.length; i++) {
            int index = i;   // 람다 내부용 final 변수

            QuestionConfig cfg = configs[i];

            // Question 객체 생성 (문제 내용, 이미지, 선택지 등)
            Question q = new Question(this, i, cfg.questionText, cfg.passage, cfg.choices, cfg.keys, cfg.hasImage ? cfg.imagePath : "");
            questions.add(q); // 리스트에 저장해 나중에 disable 처리 가능

            // 문제 카드 패널 생성 후 메인 패널에 등록
            mainPanel.add(q.createPanel(), "Q" + i);

            // 문제 번호 버튼 생성
            navButtons[i] = new JButton("Q" + (i + 1));
            navButtons[i].setFont(new Font("맑은 고딕", Font.BOLD, 15));
            navButtons[i].setMaximumSize(new Dimension(80, 40)); // 최대 크기 제한
            navButtons[i].setAlignmentX(Component.CENTER_ALIGNMENT);
            navButtons[i].setMargin(new Insets(2, 5, 2, 5)); // 버튼 안쪽 여백

            // 클릭 시 해당 문제 카드로 이동
            navButtons[i].addActionListener(e -> cardLayout.show(mainPanel, "Q" + index));

            // 답 표시 라벨 생성 (아직 선택 안 했으니 빈칸)
            answerLabels[i] = new JLabel("");
            answerLabels[i].setFont(new Font("맑은 고딕", Font.PLAIN, 18));
            answerLabels[i].setPreferredSize(new Dimension(24, 24)); // 크기 지정
            answerLabels[i].setMaximumSize(new Dimension(24, 24));
            answerLabels[i].setHorizontalAlignment(SwingConstants.CENTER);
            answerLabels[i].setVerticalAlignment(SwingConstants.CENTER);
            answerLabels[i].setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY),
                    BorderFactory.createEmptyBorder(2, 2, 2, 2)
            )); // 테두리 스타일
            answerLabels[i].setOpaque(true);
            answerLabels[i].setBackground(Color.WHITE); // 배경 하얀색

            // 문제 번호 버튼과 답 라벨을 한 줄에 배치할 패널 생성
            JPanel navRow = new JPanel();
            navRow.setLayout(new BoxLayout(navRow, BoxLayout.X_AXIS));
            navRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            navRow.setMaximumSize(new Dimension(150, 40));

            navRow.add(navButtons[i]);
            navRow.add(Box.createHorizontalStrut(4)); // 버튼과 라벨 사이 간격 띄우기
            navRow.add(answerLabels[i]);

            // 1~10번 문제는 왼쪽 컬럼에, 11번부터는 오른쪽 컬럼에 추가
            if (i < 10) {
                leftColumn.add(navRow);
            } else {
                rightColumn.add(navRow);
            }
        }

        // 제출 버튼 생성
        submitButton = new JButton("답안 제출");
        submitButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        submitButton.setMaximumSize(new Dimension(150, 50));
        submitButton.addActionListener(e -> onSubmit()); // 클릭 시 onSubmit() 호출

        // 제출 버튼 왼쪽 컬럼 맨 아래에 배치
        JPanel leftSubmitPanel = new JPanel();
        leftSubmitPanel.setLayout(new BoxLayout(leftSubmitPanel, BoxLayout.Y_AXIS));
        leftSubmitPanel.add(Box.createVerticalStrut(10));
        leftSubmitPanel.add(submitButton);
        leftSubmitPanel.add(Box.createVerticalStrut(10));
        leftColumn.add(leftSubmitPanel);

        // navPanel에 왼쪽, 오른쪽 컬럼 나란히 추가
        navPanel.add(leftColumn);
        navPanel.add(Box.createHorizontalStrut(10)); // 컬럼 사이 간격
        navPanel.add(rightColumn);

        // 프레임에 메인 문제 영역과 네비게이션 영역 붙이기
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.add(navPanel, BorderLayout.EAST);

        frame.setVisible(true); // 화면 보이기
    }

    // 답안 제출 버튼 클릭 시 실행되는 메서드
    private void onSubmit() {
        if (submitted) { // 이미 제출 완료 상태면 알림만 띄우고 종료
            JOptionPane.showMessageDialog(frame, "이미 정답을 제출했습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 모든 문제에 답이 선택됐는지 검사
        for (char c : userAnswers) {
            if (c == '\u0000') { // 아직 답 안 한 문제 있으면 경고 띄우고 종료
                JOptionPane.showMessageDialog(frame, "모든 문제를 풀어야 합니다.", "경고", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        // 제출 확인 다이얼로그 (예/아니오 선택)
        int result = JOptionPane.showConfirmDialog(frame,
                "답안을 제출하시겠습니까?\n제출 후에는 답안을 수정할 수 없습니다.",
                "답안 제출 확인",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            submitted = true;  // 제출 완료 상태로 변경

            // 모든 문제 선택지 비활성화 (답 수정 못 하게)
            for (Question q : questions) {
                q.disableChoices();
            }

            // 채점 결과 보여주기
            showResult();
        }
    }

    // 채점 후 결과 창 띄우는 메서드
    private void showResult() {
        QuestionConfig[] configs = QuestionData.QUESTIONS;
        int correctCount = 0;       // 맞은 문제 수
        int incorrectCount = 0;     // 틀린 문제 수

        List<String[]> wrongDetails = new ArrayList<>(); // 틀린 문제 번호, 내 답, 정답 저장

        for (int i = 0; i < configs.length; i++) {
            // 정답키(answer)와 사용자 답 비교
            if (userAnswers[i] == configs[i].answer) {
                correctCount++;
            } else {
                incorrectCount++;
                wrongDetails.add(new String[] {
                        String.valueOf(i + 1),
                        String.valueOf(userAnswers[i]),
                        String.valueOf(configs[i].answer)
                });
            }
        }

        // 결과 표시용 패널 생성
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BorderLayout(10, 10));

        // 상단: 맞은 개수, 틀린 개수 라벨
        JLabel correctLabel = new JLabel("맞은 개수: " + correctCount);
        correctLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        JLabel incorrectLabel = new JLabel("틀린 개수: " + incorrectCount);
        incorrectLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // 1행 2열 격자
        topPanel.add(correctLabel);
        topPanel.add(incorrectLabel);
        resultPanel.add(topPanel, BorderLayout.NORTH);

        // 하단: 틀린 문제들 테이블 생성
        String[] columnNames = {"번호", "내 답", "정답"};
        String[][] data = new String[wrongDetails.size()][3];
        for (int i = 0; i < wrongDetails.size(); i++) {
            data[i] = wrongDetails.get(i);
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // 결과창 띄우기
        JOptionPane.showMessageDialog(frame, resultPanel, "정답 결과", JOptionPane.INFORMATION_MESSAGE);
    }

    // 문제별 답 선택 시 네비게이션에 선택 답 표시 갱신
    public void updateAnswerLabel(int index) {
        char ans = userAnswers[index];
        String answerText = (ans != '\u0000') ? choiceCharToNumber(ans) : "";
        answerLabels[index].setText(answerText);
    }

    // 'A'~'E'를 ①~⑤로 변환해주는 헬퍼 메서드
    private String choiceCharToNumber(char ch) {
        return switch (ch) {
            case 'A' -> "①";
            case 'B' -> "②";
            case 'C' -> "③";
            case 'D' -> "④";
            case 'E' -> "⑤";
            default -> "";
        };
    }

    // 메인 메서드 - 프로그램 시작 지점
    public static void main(String[] args) {
        new QuizApp();
    }
}
