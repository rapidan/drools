package org.drools.repository;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

/**
 * This wraps a node iterator, and provides PackageItems when requested. This
 * supports lazy loading if needed.
 */
public class PackageIterator implements Iterator<PackageItem> {
	private final NodeIterator packageNodeIterator;
	private final RulesRepository repository;
	private boolean searchArchived = false;
	private Node current = null;
	private Node next = null;

	public PackageIterator() {
		this(null, null);
	}
	
	public PackageIterator(RulesRepository repository, NodeIterator packageNodes) {
		this.packageNodeIterator = packageNodes;
		this.repository = repository;
	}

	public boolean hasNext() {
		if (packageNodeIterator == null) {
			return false;
		}
		boolean hasnext = false;
		if (this.next == null) {
			while (this.packageNodeIterator.hasNext()) {
				Node element = (Node) this.packageNodeIterator.next();
				try {
					//Do not return Global Area
					if ((searchArchived || !element.getProperty(VersionableItem.CONTENT_PROPERTY_ARCHIVE_FLAG).getBoolean()) && !RulesRepository.RULE_GLOBAL_AREA.equals(element.getName())) {
						hasnext = true;
						this.next = element;
						break;
					}
				} catch (RepositoryException e) {
					e.printStackTrace();
				}
			}   
		} else {
			hasnext = true;
		}
		return hasnext;
	}

	public PackageItem next() {
		if (this.next == null) {
			this.hasNext();
		}

		this.current = this.next;
		this.next = null;

		if (this.current == null) {
			throw new NoSuchElementException("No more elements to return");
		}
		
		return new PackageItem(this.repository, (Node) this.current);

	}

	public void setArchivedIterator(boolean search) {
		this.searchArchived = search;
	}

	public boolean isSetArchivedSearch() {
		return this.searchArchived;
	}

	public void remove() {
		throw new UnsupportedOperationException(
				"You can not remove items this way.");
	}

}
