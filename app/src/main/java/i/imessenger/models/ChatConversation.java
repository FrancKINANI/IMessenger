package i.imessenger.models;

public class ChatConversation {
    public static final int TYPE_USER = 0;
    public static final int TYPE_GROUP = 1;
    public static final int TYPE_HEADER = 2;

    private String id;
    private String name;
    private String image;
    private int type;
    private String sectionTitle; // For headers

    // For Users
    private String email;
    private String token;

    // For Groups
    private String groupType;

    public ChatConversation() {}

    // Constructor for Header
    public ChatConversation(String sectionTitle) {
        this.type = TYPE_HEADER;
        this.sectionTitle = sectionTitle;
    }

    // Constructor for User
    public ChatConversation(User user) {
        this.type = TYPE_USER;
        this.id = user.getUid();
        this.name = user.getFullName();
        this.image = user.getProfileImage();
        this.email = user.getEmail();
        this.token = user.getFcmToken();
    }

    // Constructor for Group
    public ChatConversation(Group group) {
        this.type = TYPE_GROUP;
        this.id = group.getGroupId();
        this.name = group.getGroupName();
        this.image = group.getGroupImage();
        this.groupType = group.getGroupType();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public int getType() { return type; }
    public String getSectionTitle() { return sectionTitle; }
    public String getEmail() { return email; }
    public String getToken() { return token; }
    public String getGroupType() { return groupType; }
}
