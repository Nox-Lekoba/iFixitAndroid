package com.dozuki.ifixit.util.api;

import com.dozuki.ifixit.model.Image;
import com.dozuki.ifixit.model.Video;
import com.dozuki.ifixit.model.guide.Guide;
import com.dozuki.ifixit.model.guide.GuideStep;
import com.dozuki.ifixit.util.ImageSizes;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Stores progress information about syncing guide media.
 */
public class GuideMediaProgress {
   public ApiEvent.ViewGuide mGuideEvent;
   public Guide mGuide;
   public Set<String> mMissingMedia;
   public int mTotalMedia;
   public int mMediaRemaining;

   public GuideMediaProgress(ApiEvent.ViewGuide guideEvent, ImageSizes imageSizes) {
      this(guideEvent.getResult(), imageSizes);

      mGuideEvent = guideEvent;
   }

   public GuideMediaProgress(Guide guide, ImageSizes imageSizes) {
      mGuide = guide;
      mMissingMedia = new HashSet<String>();
      mTotalMedia = 0;

      addMediaIfMissing(mGuide.getIntroImage().getPath(imageSizes.getGrid()));

      for (GuideStep step : mGuide.getSteps()) {
         for (Image image : step.getImages()) {
            addMediaIfMissing(image.getPath(imageSizes.getMain()));

            // The counting is off because thumb is the same as getMain so we think
            // we need to download double the number of images we actually need to.
            //addMediaIfMissing(image.getPath(mImageSizes.getThumb()));

            addMediaIfMissing(image.getPath(imageSizes.getFull()));
         }

         if (step.hasVideo()) {
            Video video = step.getVideo();
            addMediaIfMissing(video.getThumbnail().getPath(imageSizes.getMain()));
            // TODO: I don't think that the order of the encodings is reliable so
            // we should pick one that we like and use that.
            addMediaIfMissing(video.getEncodings().get(0).getURL());
         }
      }

      mMediaRemaining = mMissingMedia.size();
   }

   private void addMediaIfMissing(String imageUrl) {
      // Always add to the total.
      mTotalMedia++;

      File file = new File(ApiSyncAdapter.getOfflineMediaPath(imageUrl));
      if (!file.exists()) {
         mMissingMedia.add(imageUrl);
      }
   }
}
