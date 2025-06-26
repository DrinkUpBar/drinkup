package cool.drinkup.drinkup.infrastructure.spi;

public interface SmsSender {
    void sendSms(String phoneNumber, String code);

    boolean verifySms(String phoneNumber, String code);
}
