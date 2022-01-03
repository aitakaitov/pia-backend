package backend.model.entity.utils;

import backend.model.entity.PostEntity;

import java.util.Comparator;

public class PostTimeComparator implements Comparator<PostEntity> {
    @Override
    public int compare(PostEntity o1, PostEntity o2) {
        return o1.getTime_posted().compareTo(o2.getTime_posted());
    }
}
