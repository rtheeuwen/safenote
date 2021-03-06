package nl.safenote.model;

public class Header {

	public Header(Note note) {
		this.id = note.getId();
		this.header = note.getHeader();
	}

	private final String id;
	private String header;

	public String getId() {
		return id;
	}

	public String getHeader() {
		return header;
	}

	public Header setHeader(String header) {
		if (header.equals(""))
			this.header = Note.NEWNOTEHEADER;
		else
			this.header = header;

		return this;
	}

	@Override
	public int hashCode() {
		return id.hashCode() ^ header.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof Header)) return false;
		Header other = (Header) object;
		return other.hashCode() == this.hashCode();
	}

}
