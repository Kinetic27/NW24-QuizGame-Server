import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException, IllegalArgumentException {
        final int PORT = 8000;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server Started.");

            try (ExecutorService pool = Executors.newFixedThreadPool(20)) {
                //noinspection InfiniteLoopStatement
                while (true) {
                    Socket sock = serverSocket.accept();
                    pool.execute(new Quiz(sock));
                }
            }
        }
    }

    private record Quiz(Socket socket) implements Runnable {
        @Override
        public void run() {
            System.out.println("start server");

            TreeMap<String, String> quizList = new TreeMap<>();

            quizList.put("Q1. 가천대가 있는 나라는?", "대한민국");
            quizList.put("Q2. 가천대의 개교년도는?", "1939");
            quizList.put("Q3. 가천대 총장님의 성함은?", "이길여");
            quizList.put("Q4. 가천대의 상징물은?", "바람개비");
            quizList.put("Q5. 가천대가 위치한 시는?", "성남");

            while (true) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    int score = 0;

                    for (int i = 0; i < quizList.size(); i++) {
                        String quiz = (String) quizList.keySet().toArray()[i];
                        String answer = quizList.get(quiz);

                        out.println("quiz:" + quiz + ":" + answer.length());

                        String clientMessage = in.readLine();
                        // System.out.println(clientMessage);

                        if (!clientMessage.startsWith("q_answer")) {
                            throw new IOException("answer format unavailabled.");
                        }

                        String clientAnswer = URLDecoder.decode(
                                clientMessage.split(":")[1],
                                StandardCharsets.UTF_8
                        );

                        if (clientAnswer.equals(answer)) {
                            out.println("answer:correct");
                            score++;
                        } else {
                            out.println("answer:incorrect:" + answer);
                        }
                    }

                    out.println("score:" + score);
                    socket.close();
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    break;
                }
            }
        }
    }
}