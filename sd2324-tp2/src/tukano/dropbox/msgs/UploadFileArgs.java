package tukano.dropbox.msgs;

public record UploadFileArgs(boolean autorename, String mode, boolean mute, String path, boolean strict_conflict) {
}