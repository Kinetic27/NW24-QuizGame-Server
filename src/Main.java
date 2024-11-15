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
        // set server port to 8000
        final int PORT = 8000;

        // open socket server with port
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server Started.");

            // make thread pool
            try (ExecutorService pool = Executors.newFixedThreadPool(20)) {
                // loop and make pool excute
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

            // store quiz set into TreeMap
            TreeMap<String, String> quizList = new TreeMap<>();

            quizList.put("Q1. 가천대가 있는 나라는?", "대한민국");
            quizList.put("Q2. 가천대의 개교년도는?", "1939");
            quizList.put("Q3. 가천대 총장님의 성함은?", "이길여");
            quizList.put("Q4. 가천대의 상징물은?", "바람개비");
            quizList.put("Q5. 가천대가 위치한 시는?", "성남");

            // loop for socket
            while (true) {
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                    // variable for score
                    int score = 0;

                    // loop by quiz length
                    for (int i = 0; i < quizList.size(); i++) {
                        String quiz = (String) quizList.keySet().toArray()[i];
                        String answer = quizList.get(quiz);

                        // send quiz data to client
                        out.println("quiz:" + quiz + ":" + answer.length());

                        // get quiz answer in client
                        String clientMessage = in.readLine();

                        // System.out.println(clientMessage);

                        // answer type error
                        if (!clientMessage.startsWith("q_answer")) {
                            throw new IOException("answer format unavailabled.");
                        }

                        // decode answer with URLdecoder
                        String clientAnswer = URLDecoder.decode(
                                clientMessage.split(":")[1],
                                StandardCharsets.UTF_8
                        );

                        // judge answer and send correctness to client
                        if (clientAnswer.equals(answer)) {
                            out.println("answer:correct");

                            // score add 1
                            score++;
                        } else {
                            out.println("answer:incorrect:" + answer);
                        }
                    }

                    // send final score to client
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