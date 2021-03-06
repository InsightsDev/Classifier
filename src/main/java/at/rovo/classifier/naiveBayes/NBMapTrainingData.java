package at.rovo.classifier.naiveBayes;

import at.rovo.classifier.CategoryEntry;
import at.rovo.classifier.TrainingData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A naive Bayes training data implementation which stores trained data in a nested {@link Map} structure.
 * <p/>
 * The first map will hold the categories with all their entries, while the internal map will hold each word for the
 * respective category with their number of occurrences.
 * <p/>
 * Example:<br/> <code>{'in':{'word1':num1,'word2':num2}, 'out':{'word1':num3,'word3':num4}}</code>
 * <p/>
 * The example above has two categories: <em>in</em> and <em>out</em>. <em>in</em> contains two features <em>word1</em>
 * which occurs num1 times within samples and <em>word2</em> which occurs num2 times within the trained samples.
 *
 * @param <F>
 *         The type of the features or words
 * @param <C>
 *         The type of the categories or classes
 *
 * @author Roman Vottner
 */
public class NBMapTrainingData<F, C> extends NBTrainingData<F, C>
{
    /** The logger of this class **/
    private static final Logger logger = LoggerFactory.getLogger(NBMapTrainingData.class);
    /** Unique identifier necessary for serialization **/
    private static final long serialVersionUID = -2101815681608863601L;
    /** Map containing the trained data */
    private Map<C, CategoryEntry<F, C>> categories = null;
    /** The total number of features trained **/
    private long totalNumberOfFeatures = 0L;

    /**
     * Initializes a package-private instance of an abstract training data object for a naive Bayes classifier.
     */
    NBMapTrainingData()
    {
        this.categories = new Hashtable<>();
    }

    @Override
    public void incrementFeature(F feature, C category)
    {
        CategoryEntry<F, C> cat = this.categories.get(category);
        if (cat != null)
        {
            cat.getFeatures().merge(feature, 1, (Integer oldValue, Integer defaultValue) -> oldValue + defaultValue);
        }
        else
        {
            this.categories.put(category, new CategoryEntry<>(feature));
        }
    }

    @Override
    public void incrementNumberOfSamplesForCategory(C category)
    {
        CategoryEntry<F, C> catEntry = this.categories.get(category);
        if (catEntry != null)
        {
            this.categories.put(category, catEntry.increment());
        }
        else
        {
            this.categories.put(category, new CategoryEntry<>());
        }
    }

    @Override
    protected int getNumberOfCategories()
    {
        return this.categories.size();
    }

    @Override
    protected long getTotalNumberOfFeatures()
    {
        if (this.totalNumberOfFeatures == 0L)
        {
            Set<F> features = new HashSet<>();
            for (C category : this.categories.keySet())
            {
                features.addAll(this.categories.get(category).getFeatures().keySet());
            }
            this.totalNumberOfFeatures = features.size();
        }
        return this.totalNumberOfFeatures;
    }

    @Override
    public long getNumberOfSamplesForCategory(C category)
    {
        CategoryEntry<F, C> catEntry = this.categories.get(category);
        if (catEntry != null)
        {
            return catEntry.getNumSamplesForCategory();
        }
        return 0;
    }

    @Override
    public long getTotalNumberOfSamples()
    {
        long sum = 0;
        for (CategoryEntry<F, C> entry : this.categories.values())
        {
            sum += entry.getNumSamplesForCategory();
        }
        return sum;
    }

    @Override
    public int getFeatureCount(F feature, C category)
    {
        CategoryEntry<F, C> catEntry = this.categories.get(category);
        if (catEntry != null)
        {
            Map<F, Integer> feat = catEntry.getFeatures();
            if (feat == null)
            {
                return 0;
            }
            Integer num = feat.get(feature);
            if (num != null)
            {
                return num;
            }
        }
        return 0;
    }

    @Override
    protected long getFeatureCount(F feature)
    {
        long sum = 0;
        for (C category : this.categories.keySet())
        {
            sum += this.getFeatureCount(feature, category);
        }
        return sum;
    }

    @Override
    protected boolean containsCategory(C category)
    {
        return this.categories.containsKey(category);
    }

    @Override
    protected Set<C> getCategories()
    {
        return this.categories.keySet();
    }

    @Override
    public void saveData(File directory, String name)
    {
        File dataFile = new File(directory.getAbsoluteFile(), name);
        try (ObjectOutput object = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(dataFile))))
        {
            object.writeObject(this);
            logger.info("Persisted {} successfully", dataFile);
        }
        catch (IOException e)
        {
            logger.error("Error while persisting classifier data", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean loadData(File serializedObject)
    {
        NBMapTrainingData<F, C> data = null;
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(serializedObject))))
        {
            Object obj = ois.readObject();
            if (obj instanceof TrainingData)
            {
                data = (NBMapTrainingData<F, C>) obj;
                logger.info("Found trained data for: {}", data);
            }
            else
            {
                logger.error("File is not a valid data object for this classifier!");
            }
        }
        catch (IOException | ClassNotFoundException e)
        {
            logger.error("Error while loading classifier data", e);
        }

        if (data != null)
        {
            this.categories = data.categories;
            this.totalNumberOfFeatures = data.totalNumberOfFeatures;
            return true;
        }
        return false;
    }
}
