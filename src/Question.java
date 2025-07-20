import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Question {
    private final QuizApp app;       // QuizApp 클래스 인스턴스 참조.
    private final int idx;           // 현재 문제의 인덱스 번호 (0부터 시작)
    private final String questionText; // 문제의 제목 또는 질문 텍스트.
    private final String passage;      // 문제 본문 또는 설명 텍스트.
    private final String[] choices;    // 문제의 선택지 텍스트 배열.
    private final char[] keys;         // 선택지들의 고유 키 배열. 보통 'A', 'B', 'C' 등.
    private JLabel imageLabel;         // 문제에 첨부된 이미지를 보여줄 JLabel 객체.
    private ImageIcon originalIcon;    // 원본 이미지 아이콘 객체 저장.

    private List<JRadioButton> choiceButtons = new ArrayList<>();
    // 이 문제의 선택지들을 담은 JRadioButton 리스트.
    // 답 제출 후 비활성화(disable) 작업 시 사용.

    // 생성자 - 문제 제목, 본문, 선택지, 키, 이미지 경로 등 초기화
    public Question(QuizApp app, int idx, String questionText, String passage, String[] choices, char[] keys, String imagePath) {
        this.app = app;               // QuizApp 객체 참조 저장(답 표시 갱신에 필요)
        this.idx = idx;               // 문제 번호 저장
        this.questionText = questionText; // 문제 제목 저장
        this.passage = passage;           // 문제 본문 저장
        this.choices = choices;           // 선택지 배열 저장
        this.keys = keys;                 // 선택지 키 배열 저장

        // 이미지 경로가 null이 아니고 빈 문자열이 아니면 이미지 로딩 시도
        if (imagePath != null && !imagePath.isEmpty()) {
            // 클래스패스 기반으로 이미지 리소스 경로 가져오기 (리소스 파일 위치에 따라 다름)
            java.net.URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl != null) {
                // 이미지가 존재하면 ImageIcon 생성 후 JLabel에 담기
                originalIcon = new ImageIcon(imgUrl);
                imageLabel = new JLabel(originalIcon);
            } else {
                // 이미지 경로에 파일이 없으면 에러 메시지 콘솔 출력
                System.err.println("이미지를 찾을 수 없습니다: " + imagePath);
            }
        }
    }

    // 문제를 보여줄 JPanel 생성 및 반환하는 메서드
    public JPanel createPanel() {
        JPanel panel = new JPanel();                      // 문제 화면을 담을 새 JPanel 생성
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); // 세로 방향 박스 레이아웃으로 설정 (위에서 아래로 쌓음)
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // 패널 안쪽에 10픽셀 여백 줌

        // 문제 제목 JLabel 생성 - HTML 태그로 감싸서 여러 줄 표현 가능, 볼드체, 크기 20
        panel.add(makeLabel("<html>" + questionText + "</html>", Font.BOLD, 20));
        panel.add(Box.createRigidArea(new Dimension(0, 5)));  // 제목과 다음 컴포넌트 사이 세로 간격 5픽셀

        // 이미지가 있으면 문제 이미지 JLabel 추가
        if (imageLabel != null) {
            panel.add(imageLabel);                          // 이미지 라벨 패널에 추가
            panel.add(Box.createRigidArea(new Dimension(0, 5))); // 이미지와 다음 컴포넌트 사이 간격 5픽셀
        }

        JLabel passageLabel = null; // 본문 텍스트용 JLabel 변수 선언 (없으면 null)
        if (passage != null && !passage.isEmpty()) {
            // 본문도 HTML 태그 + CSS 스타일 적용 (너비 500px, 회색 테두리, 10px 패딩)
            passageLabel = makeLabel(
                    "<html><div style='width:500px; border:1px solid gray; padding:10px;'>" + passage + "</div></html>",
                    Font.PLAIN, 15);
            panel.add(passageLabel);                       // 본문 라벨 패널에 추가
            panel.add(Box.createRigidArea(new Dimension(0, 5))); // 본문과 다음 컴포넌트 사이 5픽셀 간격
        }

        // 람다 함수 내부에서 사용하려면 final 혹은 effectively final이어야 하므로 복사본 생성
        JLabel finalPassageLabel = passageLabel;

        // 패널이 리사이즈될 때 자동으로 폰트 크기, 본문 너비, 이미지 크기 재조정하는 리스너 등록
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 현재 패널 크기 측정
                int width = panel.getWidth();
                int height = panel.getHeight();

                // 기준 디자인 크기 (이 크기를 1로 보고 비율 계산)
                int baseWidth = 800;
                int baseHeight = 500;

                // 가로, 세로 스케일 계산 (현재 크기 대비 기준 크기 비율)
                float scaleW = (float) width / baseWidth;
                float scaleH = (float) height / baseHeight;

                // 실제 적용할 스케일은 가로/세로 중 작은 값으로 (비율 유지 위해)
                float scale = Math.min(scaleW, scaleH);

                // 본문 라벨이 있으면 본문 너비도 스케일에 맞춰서 조절
                if (finalPassageLabel != null) {
                    int passageWidth = (int) (500 * scale); // 원래 500px 너비 * 스케일

                    // 본문 HTML 스타일에 너비 반영하여 다시 세팅
                    String updatedHtml = "<html><div style='width:" + passageWidth + "px; border:1px solid gray; padding:10px;'>" + passage + "</div></html>";
                    finalPassageLabel.setText(updatedHtml);
                }

                // 패널 내 각 컴포넌트에 대해 폰트 크기도 스케일 맞게 변경
                for (Component comp : panel.getComponents()) {
                    if (comp instanceof JLabel label) {
                        // JLabel 폰트 크기 20 * 스케일 배수로 변경 (제목, 본문 등)
                        label.setFont(label.getFont().deriveFont(label.getFont().getStyle(), 20 * scale));
                    } else if (comp instanceof JScrollPane sp) {
                        // JScrollPane 안에 선택지들이 있으므로 그 안도 순회하며 폰트 크기 변경
                        Component view = sp.getViewport().getView();
                        if (view instanceof JPanel innerPanel) {
                            for (Component row : innerPanel.getComponents()) {
                                if (row instanceof JPanel innerRow) {
                                    for (Component c : innerRow.getComponents()) {
                                        if (c instanceof JLabel label) {
                                            label.setFont(label.getFont().deriveFont(label.getFont().getStyle(), 20 * scale));
                                        } else if (c instanceof JRadioButton btn) {
                                            btn.setFont(btn.getFont().deriveFont(btn.getFont().getStyle(), 20 * scale));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 이미지가 있을 때는 비율 유지하며 최대 크기 제한에 맞춰 크기 조절
                if (imageLabel != null && originalIcon != null) {
                    int maxWidth = 400;  // 최대 가로 크기 (px)
                    int maxHeight = 300; // 최대 세로 크기 (px)

                    int originalWidth = originalIcon.getIconWidth();   // 원본 이미지 가로 크기
                    int originalHeight = originalIcon.getIconHeight(); // 원본 이미지 세로 크기

                    // 스케일을 적용해 새 크기 계산
                    int newWidth = (int) (originalWidth * scale);
                    int newHeight = (int) (originalHeight * scale);

                    // 최대 크기 대비 가로/세로 비율 계산
                    float widthRatio = (float) maxWidth / newWidth;
                    float heightRatio = (float) maxHeight / newHeight;

                    // 가로, 세로 제한 중 더 엄격한 제한 적용 (1보다 클 수 없음)
                    float minRatio = Math.min(1f, Math.min(widthRatio, heightRatio));

                    // 최종 크기 결정 (비율 유지)
                    newWidth = (int) (newWidth * minRatio);
                    newHeight = (int) (newHeight * minRatio);

                    // 최소 크기 1픽셀 보장 (0 이하는 안 됨)
                    newWidth = Math.max(newWidth, 1);
                    newHeight = Math.max(newHeight, 1);

                    // 이미지 부드럽게 스케일링 후 아이콘 설정
                    Image scaledImg = originalIcon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImg));
                }

                // 변경사항 반영: 레이아웃 다시 계산, 다시 그리기
                panel.revalidate();
                panel.repaint();
            }
        });

        // 선택지들은 ButtonGroup에 넣어서 한 문제 내에서 하나만 선택 가능하게 설정
        ButtonGroup group = new ButtonGroup();

        // 선택지들 담을 패널 생성, 세로 방향 박스 레이아웃
        JPanel choicesPanel = new JPanel();
        choicesPanel.setLayout(new BoxLayout(choicesPanel, BoxLayout.Y_AXIS));
        choicesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 이전에 있던 선택지 버튼 리스트 초기화 (중복 방지)
        choiceButtons.clear();

        // 각 선택지마다 반복
        for (int i = 0; i < choices.length; i++) {
            JPanel row = new JPanel();          // 선택지 한 줄용 패널 생성
            row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS)); // 가로 방향 박스 레이아웃
            row.setOpaque(false);               // 배경 투명 처리

            char choiceKey = keys[i];           // 선택지 키 (예: 'A', 'B')
            String number = choiceCharToNumber(choiceKey); // 키를 '①', '②' 형태로 변환

            // 선택지 라디오 버튼 생성 (번호 + 텍스트)
            JRadioButton choiceBtn = new JRadioButton(number + " " + choices[i]);
            choiceBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 20));  // 폰트 지정
            choiceBtn.setOpaque(false);                                // 배경 투명
            choiceBtn.setAlignmentY(Component.CENTER_ALIGNMENT);       // 세로 중앙 정렬

            int index = idx;  // 람다에서 쓰기 위해 final or effectively final 변수로 복사
            choiceBtn.addActionListener(e -> {
                // 버튼 클릭 시 해당 문제에 선택한 답안 저장
                QuizApp.userAnswers[index] = choiceKey;
                // QuizApp에 선택한 답 표시 갱신 요청
                app.updateAnswerLabel(index);
            });

            group.add(choiceBtn);  // ButtonGroup에 버튼 추가 (상호 배타적 선택 보장)
            row.add(choiceBtn);    // 선택지 한 줄 패널에 버튼 추가
            row.setAlignmentX(Component.LEFT_ALIGNMENT); // 가로 방향 왼쪽 정렬
            choicesPanel.add(row); // 선택지 전체 패널에 행 추가
            choicesPanel.add(Box.createVerticalStrut(5)); // 선택지 사이 세로 간격 5픽셀 추가

            // 선택지 버튼 리스트에 저장 (답 제출 후 비활성화 등에 사용)
            choiceButtons.add(choiceBtn);
        }

        // 선택지 패널을 스크롤 가능하도록 JScrollPane으로 감싸기
        JScrollPane scrollPane = new JScrollPane(choicesPanel);
        scrollPane.setBorder(null);                         // 스크롤 테두리 제거
        scrollPane.setPreferredSize(new Dimension(600, 250));  // 크기 고정
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // 스크롤 속도 조절

        panel.add(scrollPane);  // 최종 문제 패널에 선택지 스크롤 패널 추가

        return panel;  // 완성된 문제 화면 패널 반환
    }

    // 선택지 키 문자(A, B, C 등)를 ①, ②, ③ ... 으로 바꿔주는 헬퍼 메서드
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

    // HTML 형식 문자열을 받아 JLabel을 만드는 메서드 (글씨 스타일과 크기 지정 가능)
    private JLabel makeLabel(String html, int style, int size) {
        JLabel label = new JLabel(html);                      // JLabel 생성
        label.setFont(new Font("맑은 고딕", style, size));   // 폰트 스타일과 크기 지정
        label.setAlignmentX(Component.LEFT_ALIGNMENT);       // 좌측 정렬
        return label;
    }

    // 문제 답 제출 후 선택지 버튼 전부 비활성화 (답 수정 불가 처리)
    public void disableChoices() {
        for (JRadioButton btn : choiceButtons) {
            btn.setEnabled(false); // 각 선택지 버튼 비활성화
        }
    }

    // QuizApp에서 필요하면 이 문제의 선택지 버튼 리스트를 얻을 수 있도록 getter 제공
    public List<JRadioButton> getChoiceButtons() {
        return choiceButtons;
    }
}
