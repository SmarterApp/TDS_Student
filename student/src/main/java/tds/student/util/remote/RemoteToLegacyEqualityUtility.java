/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.util.remote;

import java.util.List;

import tds.student.services.data.ItemResponse;
import tds.student.services.data.PageGroup;

/**
 * This is a simple equality utility class to compare objects created by legacy
 * code and those created from remote objects.  The existing domain objects have
 * a different equals/hashcode implementation which at this point we're afraid
 * to modify
 */
public class RemoteToLegacyEqualityUtility {

  public static boolean isItemResponsesEqual(final List<ItemResponse> legacy, final List<ItemResponse> remote) {
    if(legacy.size() != remote.size()) return false;

    for(ItemResponse legacyResponse : legacy) {
      for(ItemResponse remoteResponse : remote) {
        if(!isItemResponseEqual(legacyResponse, remoteResponse)) {
          return false;
        }
      }
    }

    return true;
  }

  public static boolean isItemResponseEqual(final ItemResponse legacy, final ItemResponse remote) {
    if (legacy.getBankKey() != remote.getBankKey()) return false;
    if (legacy.getItemKey() != remote.getItemKey()) return false;
    if (legacy.getSegment() != remote.getSegment()) return false;
    if (legacy.getPage() != remote.getPage()) return false;
    if (legacy.getPosition() != remote.getPosition()) return false;
    if (legacy.isMarkForReview() != remote.isMarkForReview()) return false;
    if (legacy.getSequence() != remote.getSequence()) return false;
    if (legacy.getIsSelected() != remote.getIsSelected()) return false;
    if (legacy._isRequired != remote._isRequired) return false;
    if (legacy._scorePoint != remote._scorePoint) return false;
    if (legacy._isVisible != remote._isVisible) return false;
    if (legacy._isValid != remote._isValid) return false;
    if (legacy._prefetched != remote._prefetched) return false;
    if (legacy._isPrintable != remote._isPrintable) return false;
    if (legacy._printed != remote._printed) return false;
    if (legacy.getSegmentID() != null ? !legacy.getSegmentID().equals(remote.getSegmentID()) : remote.getSegmentID() != null) return false;
//    if (_dateCreated != null ? !_dateCreated.equals(that._dateCreated) : that._dateCreated != null) return false;
    if (legacy.getFormat() != null ? !legacy.getFormat().equals(remote.getFormat()) : remote.getFormat() != null) return false;
    if (legacy.getValue()!= null ? !legacy.getValue().equals(remote.getValue()) : remote.getValue() != null) return false;
    if (legacy._scoreMark != null ? !legacy._scoreMark.equals(remote._scoreMark) : remote._scoreMark != null) return false;
    if (legacy._filePath != null ? !legacy._filePath.equals(remote._filePath) : remote._filePath != null) return false;
    return legacy._document != null ? legacy._document.equals(remote._document) : remote._document == null;
  }

  public static boolean isPageGroupEqual(final PageGroup legacy, final PageGroup remote) {
    if (legacy.getNumber() != remote.getNumber()) return false;
    if (legacy.getSegmentPos() != remote.getSegmentPos()) return false;
    if (legacy.getNumRequired() != remote.getNumRequired()) return false;
    if (legacy._printed != remote._printed) return false;
    if (legacy._prefetched != remote._prefetched) return false;
    if (legacy.getGroupID() != null ? !legacy.getGroupID().equals(remote.getGroupID()) : remote.getGroupID() != null) return false;
    if (legacy.getSegmentID() != null ? !legacy.getSegmentID().equals(remote.getSegmentID()) : remote.getSegmentID() != null) return false;
    if (legacy.getFilePath() != null ? !legacy.getFilePath().equals(remote.getFilePath()) : remote.getFilePath() != null) return false;
    if (legacy.getId() != null ? !legacy.getId().equals(remote.getId()) : remote.getId() != null) return false;
    if (legacy._document!= null ? !legacy._document.equals(remote._document) : remote._document != null) return false;
    if (legacy._iitsDocument != null ? legacy._iitsDocument.equals(remote._iitsDocument) : remote._iitsDocument != null) return false;

    return isItemResponsesEqual(legacy, remote);
  }
}
