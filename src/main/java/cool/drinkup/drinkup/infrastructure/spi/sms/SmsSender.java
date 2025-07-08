package cool.drinkup.drinkup.infrastructure.spi.sms;

public interface SmsSender {
    void sendSms(String phoneNumber, String code);

    boolean verifySms(String phoneNumber, String code);
}
