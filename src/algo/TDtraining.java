/*
1. 导入上一次训练的NN(value function)
2、for each episode(10次):
    随机选择一个棋谱
    随机选择一个中期的棋面作为初始棋面
    self-play until 棋面终止：
        使用alpha-beta返回下一步的走法，记录每个棋面的value
        每隔8步，使用Td backup更新8步以前的state value
        如果棋面终止：
            win : reward = 1
            loss : reward = 0
            棋面未终止时，reward为0
    将所有下棋过程中的state 和value保存下来
3、将state value 放到NN中重新训练
4、重复上述过程，直到象棋的棋力提升到一定水平
*/

package algo;

import game.BoardPosition;
import game.Game;
import java.util.*;
import constants.Colors;

public class TDtraining(){
    private int nsteps = 8;
    public static final double gamma = 0.7;
    public static final double lambda = 0.7;
    private final double alpha = 0.2;
    private Map<Game, Double> ValueMap = new HashMap<Game, Double>();

    public TDtraning(){}

    public TDlearn(Neuroph NN, String record){
        AlphaBeta engine = new AlphaBeta();

        Random rand = new Random();
        int numMoves = record.length() / 4;
        int step = rand.nextInt(numMoves-13) + 5；
        Game game = new Game();
        game.initBoard(record,step);

        boolean gameflag = false;
        int nstep = 0;
        int reward = 0;
        //如何判断游戏终止
        List<List<Double>> feat = Feature.featureExtractor(game);
        double[] array = Feature.featureArray(feat);
        double value = neuroph.evaluate(array);
        ValueMap.put(game.copy(), value);
        while(!gameflag){
            boolean redTurn = game.isRedTurn();
            if(redTurn){
                //alpha-beta 返回from to
                game.movePiece(from, to);
                List<List<Double>> feat = Feature.featureExtractor(game);
                double[] array = Feature.featureArray(feat);
                double value = neuroph.evaluate(array);
                ValueMap.put(game.copy(), value);
            }
            else{
                //alpha-beta 返回from to
                game.movePiece(from, to);
                List<List<Double>> feat = Feature.featureExtractor(game);
                double[] array = Feature.featureArray(feat);
                double value = neuroph.evaluate(array);
                ValueMap.put(game.copy(), value);
            }
            //判断游戏是否终止，调整gameflag
            if(gameflag){
                Colors winner = game.getWinner();
                if(winner == Colors.RED){
                    reward = 1;
                }
                else{
                    reward  = -1;
                }
            }
        }
        int size = ValueMap.size();
        List<Double> values = new ArrayList<Double>(ValueMap.values());
        Set set =  ValueMap.keySet();
        Iterator keys = set.iterator();
        for(int i=0;i<size-8;i++){
            ValueMap.put(keys.next(), (1-alpha)*values.get(i)+Math.pow(gamma,8)*values.get(i+7))
        }
        ValueMap.put(keys.next(), (1-alpha)*values.get(i)+(1-alpha)*Math.pow(gamma,8)*values.get(i+7)+(1-alpha)*reward)
        return ValueMap
    }

    public static void main(strings[] *args){
            path = '../'
            Neuroph NN = new Neuroph(path);
            for(int i=0;i <10;i++){
                // 随机选择一个棋谱
                Map<Game, Double> Valuegame = new HashMap<Game, Double>();
                Valuegame = TDtrain(NN,record);
                //将valuegame中game保存为feature和value
            }
            //重新训练NN
    }

}
