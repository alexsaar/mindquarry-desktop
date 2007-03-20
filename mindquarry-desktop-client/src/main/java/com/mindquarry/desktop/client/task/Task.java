/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.mindquarry.desktop.client.task;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/**
 * @author <a href="mailto:lars(dot)trieloff(at)mindquarry(dot)com">Lars
 *         Trieloff</a>
 */
public class Task {
    public static final String STATUS_NEW = "new"; //$NON-NLS-1$

    public static final String STATUS_RUNNING = "running"; //$NON-NLS-1$

    public static final String STATUS_PAUSED = "paused"; //$NON-NLS-1$

    public static final String STATUS_DONE = "done"; //$NON-NLS-1$

    public static final String PRIORITY_LOW = "low"; //$NON-NLS-1$

    public static final String PRIORITY_MEDIUM = "medium"; //$NON-NLS-1$

    public static final String PRIORITY_IMPORTANT = "important"; //$NON-NLS-1$

    public static final String PRIORITY_CRITICAL = "critical"; //$NON-NLS-1$

    private String id;

    private String title;

    private String status;

    private String priority;

    private String summary;

    private String description;

    private String date;
    
    private List<Person> people;
    
    private List<Dependency> dependencies;
    
    class Person {
        public String pid;
        public String role;
    }
    
    class Dependency {
        public String tid;
        public String role;
    }
    
    public void addPerson(String pid, String role) {
        Person person = new Person();
        person.pid = pid;
        person.role = role;
        people.add(person);
    }
    
    public void addDependency(String tid, String role) {
        Dependency dependency = new Dependency();
        dependency.tid = tid;
        dependency.role = role;
        dependencies.add(dependency);
    }

    public Task() {
        people = new ArrayList<Person>();
        dependencies = new ArrayList<Dependency>();
    }

    public Task(String id, String title, String status) {
        super();
        
        this.id = id;
        this.title = title;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Task are equal if there IDs are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            if (((Task) obj).id.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public Document getContentAsXML() {
        Document doc = DocumentHelper.createDocument();
        Element task = doc.addElement("task"); //$NON-NLS-1$

        Element title = task.addElement("title"); //$NON-NLS-1$
        title.setText(getTitle());

        if ((getPriority() != null) && (!getPriority().equals(""))) { //$NON-NLS-1$
            Element priority = task.addElement("priority"); //$NON-NLS-1$
            priority.setText(getPriority());
        }
        if ((getSummary() != null) && (!getSummary().equals(""))) { //$NON-NLS-1$
            Element summary = task.addElement("summary"); //$NON-NLS-1$
            summary.setText(getSummary());
        }
        if ((getStatus() != null) && (!getStatus().equals(""))) { //$NON-NLS-1$
            Element status = task.addElement("status"); //$NON-NLS-1$
            status.setText(getStatus());
        }
        if ((getDate() != null) && (!getDate().equals(""))) { //$NON-NLS-1$
            Element date = task.addElement("date"); //$NON-NLS-1$
            date.setText(getDate());
        }
        if ((getDescription() != null) && (!getDescription().equals(""))) { //$NON-NLS-1$
            Element description = task.addElement("description"); //$NON-NLS-1$
            description.setText(getDescription());
        }
        if(people.size() >0) {
            int count = 0;
            Element peopleEl = task.addElement("people"); //$NON-NLS-1$
            for(Person person : people) {
                Element itemEl = peopleEl.addElement("item");
                itemEl.addAttribute("position", String.valueOf(count));
                
                Element personEl = itemEl.addElement("person");
                personEl.addText(person.pid);
                Element roleEl = itemEl.addElement("role");
                roleEl.addText(person.role);
                
                count++;
            }
        }
        if(dependencies.size() >0) {
            int count = 0;
            Element dependenciesEl = task.addElement("dependencies"); //$NON-NLS-1$
            for(Dependency dependency : dependencies) {
                Element itemEl = dependenciesEl.addElement("item"); //$NON-NLS-1$
                itemEl.addAttribute("position", String.valueOf(count)); //$NON-NLS-1$
                
                Element taskEl = itemEl.addElement("task"); //$NON-NLS-1$
                taskEl.addText(dependency.tid);
                Element roleEl = itemEl.addElement("role"); //$NON-NLS-1$
                roleEl.addText(dependency.role);
                
                count++;
            }
        }
        return doc;
    }
}