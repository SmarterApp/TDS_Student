/*******************************************************************************
 * Educational Online Test Delivery System 
 * Copyright (c) 2014 American Institutes for Research
 *     
 * Distributed under the AIR Open Source License, Version 1.0 
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 ******************************************************************************/
package tds.student.services.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.Transformer;

import com.fasterxml.jackson.annotation.JsonProperty;

import AIR.Common.collections.IGrouping;

//import tds.itemrenderer.data.IITSDocument;
import tds.student.sql.data.OpportunityItem;

/**
 * @author temp_rreddy
 * 
 */
public class PageList extends ArrayList<PageGroup>
{
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public PageList ()
  {
    super ();
  }

  public static PageList Create (List<OpportunityItem> responses)
  {
    PageList pageList = new PageList ();

    Transformer groupTransformer = new Transformer ()
    {

      @Override
      public Object transform (Object itsDocument) {
        return ((OpportunityItem) itsDocument).getPage ();
      }
    };

    List<IGrouping<Integer, OpportunityItem>> returnList = IGrouping.<Integer, OpportunityItem> createGroups (responses, groupTransformer);

    Collections.sort (returnList, new Comparator<IGrouping<Integer, OpportunityItem>> ()
    {
      @Override
      public int compare (IGrouping<Integer, OpportunityItem> o1, IGrouping<Integer, OpportunityItem> o2) {
        return o1.getKey ().compareTo (o2.getKey ());
      }
    });

    for (IGrouping<Integer, OpportunityItem> responseGroup : returnList)
    {
      PageGroup pageGroup = PageGroup.Create (responseGroup);
      pageList.add (pageGroup);
    }

    return pageList;
  }

  @JsonProperty ("CountResponses")
  public int getCountResponses ()
  {
    // return this.Sum(g => g.Count);
    List<PageGroup> listOfPageGroup = getListOfPageGroup ();
    return listOfPageGroup.size ();
  }

  // / <summary>
  // / Gets a response group by page number
  // / </summary>
  public PageGroup getPage (int page)
  {
    // TODO
    // PageGroup pageGroup = this.Find(g => g.Number == page);
    // return pageGroup;
    List<PageGroup> listOfPageGroup = null;
    listOfPageGroup = getListOfPageGroup ();
    for (PageGroup pageGroup : listOfPageGroup) {
      if (pageGroup.getNumber () == page)
        return pageGroup;
    }
    return null;
  }

  // / <summary>
  // / Gets a response by position number
  // / </summary>
  public ItemResponse getResponse (int position)
  {
    for (PageGroup group : this)
    {
      for (ItemResponse response : group)
      {
        if (response.getPosition () == position)
          return response;
      }
    }

    return null;
  }

  // / <summary>
  // / Checks each group to determine if the number of responses required has
  // been met.
  // / </summary>
  @JsonProperty ("AllCompleted")
  public boolean isAllCompleted ()
  {
    // TODO
     return this.all(/*responseGroup => responseGroup.IsCompleted*/ new Predicate()
    {
      
      @Override
      public boolean evaluate (Object object) {
        PageGroup responseGroup = (PageGroup)object;
        return responseGroup.getIsCompleted ();
      }
    });
     //TODO mpatel - Remove following code after code review
  /*  List<PageGroup> listOfPageGroup = null;
    listOfPageGroup = getListOfPageGroup ();
    for (PageGroup pageGroup : listOfPageGroup) {
      if (pageGroup.getIsCompleted ())
        listOfPageGroup.add (pageGroup);
    }
    setListOfPageGroup (listOfPageGroup);
    return true;*/
  }

  // TODO
  public List<ItemResponse> getResponses ()
  {
    List<ItemResponse> itemResponseList = new ArrayList<ItemResponse> ();
    for (PageGroup pageGroup : this)
    {
      for (ItemResponse itemResponse : pageGroup)
      {
        itemResponseList.add (itemResponse);
      }
    }
    return itemResponseList;
  }

  /**
   * 
   * @param predicate which takes ItemResponse as Input and returns boolean
   * @return PageList
   */
  public PageList filter (Predicate predicate)
  {
    PageList pageList = new PageList ();

    List<ItemResponse> filteredItemResponse = new ArrayList<ItemResponse>();
    CollectionUtils.select (this.getResponses (), predicate,filteredItemResponse);
    for (ItemResponse itemResponse : filteredItemResponse)
    {
      // check if we have seen this page before
      PageGroup pageGroup = pageList.getPage (itemResponse.getPage ());

      // if we haven't seen the page create it
      if (pageGroup == null) {
        pageGroup = (PageGroup) getPage (itemResponse.getPage ()).clone ();
        pageList.add (pageGroup);
      }

      // add response
      pageGroup.add (itemResponse);
    }

    return pageList;
  }

  public List<PageGroup> getListOfPageGroup () {
    return this;
  }

  public void setListOfPageGroup (List<PageGroup> listOfPageGroup) {
    this.addAll (listOfPageGroup);
  }

  public PageGroup getLastOrDefault() {
    PageGroup lastOrDefault = null;
    if(!this.isEmpty ()) {
      return this.get (this.size ()-1);
    }
    return lastOrDefault;
  }
  
  private boolean all(Predicate predicate){
    int size = this.size ();
    int matchingPredicateCount = CollectionUtils.countMatches (this, predicate);
    return   size ==   matchingPredicateCount;
  }
  
}
