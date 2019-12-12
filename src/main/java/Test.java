import java.util.Arrays;
import java.util.List;

public class Test {
    public static void main(String ... args) {
        A[][] arr = new A[3][5];
        for (int i=0;i<3; i++){
            for (int j=0;j<5;j++) {
                arr[i][j] = new A();
                arr[i][j].val = i*j;
            }
        }

        int sum = 0;
        for (int i=0;i<3; i++)
            for (int j=0;j<5;j++) sum+= arr[i][j].val;

        System.out.println(sum);
    }

    public static class A{
        int val;
    }
}
