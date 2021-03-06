package at.rovo.classifier;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represents an entry of a certain category.
 *
 * @param <F>
 *         The type of the features or words
 * @param <C>
 *         The type of the categories or classes
 */
@SuppressWarnings("unused")
public class CategoryEntry<F, C> implements Serializable
{
    private static final long serialVersionUID = -1510312537959434628L;
    private Long numSamplesForCategory = 0L;
    private Map<F, Integer> features = null;

    public CategoryEntry()
    {
        this.numSamplesForCategory = 0L;
        features = new Hashtable<>();
    }

    public CategoryEntry(F feature)
    {
        this.numSamplesForCategory = 0L;
        features = new Hashtable<>();
        features.put(feature, 1);
    }

    public CategoryEntry(Long numSamplesForCategory, Map<F, Integer> features)
    {
        this.numSamplesForCategory = numSamplesForCategory;
        this.features = features;
    }

    public void setNumSamplesForCategory(Long value)
    {
        this.numSamplesForCategory = value;
    }

    public void setFeatures(Map<F, Integer> features)
    {
        this.features = features;
    }

    public Long getNumSamplesForCategory()
    {
        return this.numSamplesForCategory;
    }

    public Map<F, Integer> getFeatures()
    {
        return this.features;
    }

    public CategoryEntry<F, C> increment()
    {
        this.numSamplesForCategory++;
        return this;
    }
}
