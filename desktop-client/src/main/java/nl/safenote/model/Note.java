package nl.safenote.model;


import nl.safenote.utils.textsearch.TextSearchable;
import nl.safenote.utils.textsearch.Text;

public class Note implements TextSearchable, Cloneable {

	public final static String NEWNOTEHEADER = "New note...";

	public enum ContentType {TEXT, IMAGE}

	private String id;

	@Text(weight = 3)
	private String header;

	@Text(weight = 1)
	private String content;

	private transient boolean encrypted;
	private long modified;
	private long created;
	private long version;
	private ContentType contentType;
	private boolean deleted;
	private String hash;

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	public Note(String id, ContentType contentType) {
		if (id == null || contentType == null)
			throw new IllegalArgumentException("Constructor parameter cannot be null");
		this.id = id;
		this.header = "";
		this.content = "";
		this.contentType = contentType;
		this.modified = this.created = System.currentTimeMillis();
	}

	public void updateHeader() {
		if (encrypted)
			throw new IllegalArgumentException("note is encrypted");
		int index = content.indexOf("\n");
		index = index != -1 ? index : content.indexOf(" ");
		index = index != -1 ? index : content.length() <= 10 ? content.length() : 10;
		index = index > 35 ? 35 : index;
		header = content.substring(0, index);
	}

	public String getId() {
		return id;
	}

	public String getHeader() {
		if (encrypted)
			return header;
		else
			return header.equals("") ? NEWNOTEHEADER : header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public long getModified() {
		return modified;
	}

	public long getCreated() {
		return created;
	}

	public long getVersion() {
		return version;
	}

	public ContentType getContentType() {
		return contentType;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}


	@Override
	public int hashCode() {
		return 13 ^ id.hashCode() ^ content.hashCode() ^ header.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof Note)) return false;
		Note other = (Note) object;
		return (other.id.equals(this.id) && other.content.equals(this.content) && (other.header.equals(this.header)));
	}
}



