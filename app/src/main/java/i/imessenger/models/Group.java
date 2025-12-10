package i.imessenger.models;

import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private String groupImage;
    private String groupType; // "CLASS", "CLUB", "ALUMNI"
    private List<String> members;
    private List<String> admins;
    private List<String> blockedMembers;

    public Group() {
        // Required for Firestore
    }

    public Group(String groupId, String groupName, String groupImage, String groupType, List<String> members, List<String> admins, List<String> blockedMembers) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupImage = groupImage;
        this.groupType = groupType;
        this.members = members;
        this.admins = admins;
        this.blockedMembers = blockedMembers;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getAdmins() {
        return admins;
    }

    public void setAdmins(List<String> admins) {
        this.admins = admins;
    }

    public List<String> getBlockedMembers() {
        return blockedMembers;
    }

    public void setBlockedMembers(List<String> blockedMembers) {
        this.blockedMembers = blockedMembers;
    }
}
