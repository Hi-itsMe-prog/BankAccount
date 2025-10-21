import java.util.Random;

class Account {
    private int balance = 0;

    // Метод для пополнения баланса
    public synchronized void add(int sum) {
        balance += sum;
        System.out.println("Пополнение: +" + sum + " руб. Баланс: " + balance + " руб.");
        notifyAll(); // Уведомляем все ожидающие потоки
    }

    // Метод для снятия денег
    public synchronized void takeMoney(int sum) {
        balance -= sum;
        System.out.println("Снятие: -" + sum + " руб. Баланс: " + balance + " руб.");
    }

    // Метод для ожидания пополнения до нужной суммы
    public synchronized void wait(int targetSum) throws InterruptedException {
        System.out.println("Ожидаем накопления " + targetSum + " руб. для снятия...");

        while (balance < targetSum) {
            wait(); // Ждем, пока баланс не достигнет целевой суммы
        }

        System.out.println("Целевая сумма " + targetSum + " руб. достигнута!");
    }

    // Геттер для баланса
    public synchronized int getBalance() {
        return balance;
    }
}

// Поток для пополнения счета
class Deposit extends Thread {
    private Account account;
    private Random random = new Random();

    public Deposit(Account account) {
        this.account = account;
    }

    @Override
    public void run() {
        try {
            // Многократно пополняем счет случайными суммами
            for (int i = 0; i < 10; i++) {
                int amount = random.nextInt(100) + 1; // Случайная сумма от 1 до 100
                account.add(amount);
                Thread.sleep(500); // Пауза между пополнениями
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// Главный класс
public class BankAccountDemo {
    public static void main(String[] args) {
        Account account = new Account();

        // Запускаем поток для пополнения счета
        Deposit depositThread = new Deposit(account);
        depositThread.start();

        try {
            // Ждем, пока на счету не будет 200 рублей
            account.wait(200);

            // Снимаем 200 рублей
            account.takeMoney(200);

            // Выводим итоговый баланс
            System.out.println("Итоговый баланс: " + account.getBalance() + " руб.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}