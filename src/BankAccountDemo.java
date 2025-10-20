import java.util.Random;

class Account {
    private int balance = 0;

    // Метод для пополнения баланса
    public synchronized void deposit(int amount) {
        balance += amount;
        System.out.println("Пополнение: +" + amount + " руб. Баланс: " + balance + " руб.");
        notifyAll(); // Уведомляем все ожидающие потоки
    }

    // Метод для снятия денег
    public synchronized void withdraw(int amount) {
        balance -= amount;
        System.out.println("Снятие: -" + amount + " руб. Баланс: " + balance + " руб.");
    }

    // Метод для ожидания пополнения до нужной суммы
    public synchronized void waitForAmount(int targetAmount) throws InterruptedException {
        System.out.println("Ожидаем накопления " + targetAmount + " руб. для снятия...");

        while (balance < targetAmount) {
            wait(); // Ждем, пока баланс не достигнет целевой суммы
        }

        System.out.println("Целевая сумма " + targetAmount + " руб. достигнута!");
    }

    // Геттер для баланса
    public synchronized int getBalance() {
        return balance;
    }
}

// Поток для пополнения счета
class DepositThread extends Thread {
    private Account account;
    private Random random = new Random();

    public DepositThread(Account account) {
        this.account = account;
    }

    @Override
    public void run() {
        try {
            // Многократно пополняем счет случайными суммами
            for (int i = 0; i < 10; i++) {
                int amount = random.nextInt(100) + 1; // Случайная сумма от 1 до 100
                account.deposit(amount);
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
        DepositThread depositThread = new DepositThread(account);
        depositThread.start();

        try {
            // Ждем, пока на счету не будет 200 рублей
            account.waitForAmount(200);

            // Снимаем 200 рублей
            account.withdraw(200);

            // Выводим итоговый баланс
            System.out.println("Итоговый баланс: " + account.getBalance() + " руб.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}