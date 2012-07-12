package org.jasig.portlet.contacts.control;

import java.io.IOException;
import java.util.*;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.contacts.domains.ContactDomain;
import org.jasig.portlet.contacts.model.Contact;
import org.jasig.portlet.contacts.model.ContactSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.portlet.bind.annotation.ResourceMapping;

/**
 *
 * @author Chris White <christopher.white@manchester.ac.uk>
 */
@Controller
@RequestMapping("VIEW")
public class AutoCompleteController {

    private static Log log = LogFactory.getLog(AutoCompleteController.class);


    @ModelAttribute("domain")
    public ContactDomain getDomain(
            @RequestParam("domain") String domain
    ) {
        
        for (ContactDomain domainObj : contactDomains) {
            if (domainObj.getId().equals(domain)) {
                return domainObj;
            }
        }

        return null;
    }

    
    @ResourceMapping("autocomplete")
    public String searchDomain(
            ResourceRequest request,
            ResourceResponse response,
            @RequestParam("term") String term,
            @RequestParam(value="filter", required=false) String filter,
            @ModelAttribute("domain") ContactDomain domainObj,
            Model model) {


        log.debug("filter is " + filter);

            log.debug("q is " + term);

            ContactSet contacts = domainObj.search(term, filter);
            
            Map<String, Integer> countedResults = getCountedSearchResults(contacts);
            
            if (countedResults.size() > 0) {
                List<Map<String,String>> data = new ArrayList<Map<String,String>>();
                for (String key : countedResults.keySet()) {
                    
                    Map<String,String> entry = new HashMap<String,String>();
                    if (countedResults.get(key) > 1)
                        entry.put("label", "<span>" + countedResults.get(key) + " results</span>" + key);
                    else
                        entry.put("label", key);
                    entry.put("value", key);
                    data.add(entry);
                }
                model.addAttribute("data",data.toArray());
            }
        
        return "JSONView";

    }
    
    
    private Map<String, Integer> getCountedSearchResults(ContactSet results) {
        Map<String, Integer> countedResults = new LinkedHashMap<String, Integer>();

        for (Contact contact : results) {
            String value = contact.getFirstname() + " " + contact.getSurname();
            Integer count = countedResults.get(value);
            if (count == null) {
                count = 0;
            }
            countedResults.put(value, ++count);
        }
        return countedResults;
    }
    
    private Set<ContactDomain> contactDomains;

    @Autowired
    public void setContactDomains(Set<ContactDomain> domains) {
        contactDomains = domains;
    }

}
