package minecraft.rolest.utils;

public class NameClient {
    public static class User {
        public static String nameUser() {
            return ClientDataReader.getUsername();
        }

        public static String getIdUser1() {
            return ClientDataReader.getUserId();
        }

        public static String getTitleUSer1() {
            return ClientDataReader.getTitle();
        }

        public static String getFullInfo() {
            return ClientDataReader.getUsername() + " [" + ClientDataReader.getTitle() + "]";
        }
    }
}