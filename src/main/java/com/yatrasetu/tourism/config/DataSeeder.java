package com.yatrasetu.tourism.config;

import com.yatrasetu.tourism.entity.Destination;
import com.yatrasetu.tourism.entity.Hotel;
import com.yatrasetu.tourism.entity.Review;
import com.yatrasetu.tourism.repository.DestinationRepository;
import com.yatrasetu.tourism.repository.HotelRepository;
import com.yatrasetu.tourism.repository.ReviewRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Seeds a broad, real set of Indian destinations (heritage, hills, beaches,
 * spiritual, wildlife, offbeat) spanning many states, plus hotels and
 * reviews for a subset of the most-visited ones.
 *
 * Runs on every startup but is fully idempotent: each destination is looked
 * up by exact name first, so re-running never creates duplicates, and
 * expanding this list later (as we just did) safely adds only what's new to
 * a database that already has earlier seed data in it.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private final DestinationRepository destinationRepository;
    private final HotelRepository hotelRepository;
    private final ReviewRepository reviewRepository;

    // Reused, known-working Unsplash images, mapped by category, so every
    // new destination gets a real working photo without guessing new URLs.
    private static final String IMG_HERITAGE_1 = "https://images.unsplash.com/photo-1599661046289-e31897846e41?q=80&w=1200";
    private static final String IMG_HERITAGE_2 = "https://images.unsplash.com/photo-1600100397608-f256a01b0abe?q=80&w=1200";
    private static final String IMG_HILLS = "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?q=80&w=1200";
    private static final String IMG_BEACH_1 = "https://images.unsplash.com/photo-1590123047424-1a74a2058afe?q=80&w=1200";
    private static final String IMG_BEACH_2 = "https://images.unsplash.com/photo-1589979481223-deb893043163?q=80&w=1200";
    private static final String IMG_SPIRITUAL = "https://images.unsplash.com/photo-1561361058-c24cecae35ca?q=80&w=1200";
    private static final String IMG_WILDLIFE = "https://images.unsplash.com/photo-1602491453631-e2a5ad90a131?q=80&w=1200";
    private static final String IMG_OFFBEAT = "https://images.unsplash.com/photo-1626621331169-5f34be280831?q=80&w=1200";

    public DataSeeder(DestinationRepository destinationRepository, HotelRepository hotelRepository,
                       ReviewRepository reviewRepository) {
        this.destinationRepository = destinationRepository;
        this.hotelRepository = hotelRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    public void run(String... args) {
        // ---- Heritage ----
        Destination jaipur = ensureDestination("Jaipur", "Rajasthan", "heritage",
                "The Pink City of amber forts and mirrored palaces", 2200, IMG_HERITAGE_1, "Oct – Mar", 26.9124, 75.7873, true);
        Destination hampi = ensureDestination("Hampi", "Karnataka", "heritage",
                "Boulder-strewn ruins of a vanished empire", 1400, IMG_HERITAGE_2, "Oct – Feb", 15.3350, 76.4600, false);
        Destination agra = ensureDestination("Agra", "Uttar Pradesh", "heritage",
                "Home to the Taj Mahal and the Agra Fort on the Yamuna", 2400, IMG_HERITAGE_1, "Oct – Mar", 27.1751, 78.0421, true);
        Destination khajuraho = ensureDestination("Khajuraho", "Madhya Pradesh", "heritage",
                "Intricately carved temples from the Chandela dynasty", 1900, IMG_HERITAGE_2, "Oct – Mar", 24.8318, 79.9199, false);
        Destination mahabalipuram = ensureDestination("Mahabalipuram", "Tamil Nadu", "heritage",
                "Shore temples and rock-cut caves on the Coromandel coast", 1700, IMG_HERITAGE_1, "Nov – Feb", 12.6269, 80.1927, false);
        Destination fatehpurSikri = ensureDestination("Fatehpur Sikri", "Uttar Pradesh", "heritage",
                "A perfectly preserved Mughal capital, abandoned for centuries", 1600, IMG_HERITAGE_2, "Oct – Mar", 27.0940, 77.6711, false);
        Destination orchha = ensureDestination("Orchha", "Madhya Pradesh", "heritage",
                "Riverside cenotaphs and a fort town frozen in the 1600s", 1500, IMG_HERITAGE_1, "Oct – Mar", 25.3521, 78.6410, false);
        Destination hyderabad = ensureDestination("Hyderabad", "Telangana", "heritage",
                "Charminar, Golconda Fort and legendary biryani", 2100, IMG_HERITAGE_2, "Oct – Feb", 17.3616, 78.4747, false);
        Destination chittorgarh = ensureDestination("Chittorgarh", "Rajasthan", "heritage",
                "India's largest fort, steeped in Rajput history", 1600, IMG_HERITAGE_1, "Oct – Mar", 24.8887, 74.6269, false);

        // ---- Hills ----
        Destination munnar = ensureDestination("Munnar", "Kerala", "hills",
                "Rolling tea gardens folded into misty hills", 1800, IMG_HILLS, "Sep – May", 10.0889, 77.0595, true);
        Destination shimla = ensureDestination("Shimla", "Himachal Pradesh", "hills",
                "Colonial-era hill capital along a pine-covered ridge", 2600, IMG_HILLS, "Mar – Jun", 31.1048, 77.1734, true);
        Destination manali = ensureDestination("Manali", "Himachal Pradesh", "hills",
                "Gateway to the high Himalayas and Solang Valley", 2800, IMG_HILLS, "Mar – Jun", 32.2432, 77.1892, true);
        Destination darjeeling = ensureDestination("Darjeeling", "West Bengal", "hills",
                "Tea estates, toy trains and Kanchenjunga sunrises", 2200, IMG_HILLS, "Mar – May", 27.0410, 88.2663, false);
        Destination ooty = ensureDestination("Ooty", "Tamil Nadu", "hills",
                "The Queen of the Nilgiris, with botanical gardens and lakes", 2000, IMG_HILLS, "Oct – Jun", 11.4064, 76.6932, false);
        Destination coorg = ensureDestination("Coorg", "Karnataka", "hills",
                "Coffee plantations and waterfalls in Karnataka's Western Ghats", 2300, IMG_HILLS, "Oct – Mar", 12.4244, 75.7382, false);
        Destination gangtok = ensureDestination("Gangtok", "Sikkim", "hills",
                "Monasteries and Himalayan views above the clouds", 2500, IMG_HILLS, "Mar – Jun", 27.3389, 88.6065, false);
        Destination nainital = ensureDestination("Nainital", "Uttarakhand", "hills",
                "A lake town ringed by forested hills", 2000, IMG_HILLS, "Mar – Jun", 29.3919, 79.4542, false);
        Destination mussoorie = ensureDestination("Mussoorie", "Uttarakhand", "hills",
                "The 'Queen of the Hills' overlooking the Doon valley", 2100, IMG_HILLS, "Mar – Jun", 30.4598, 78.0664, false);
        Destination kodaikanal = ensureDestination("Kodaikanal", "Tamil Nadu", "hills",
                "A misty lake town amid eucalyptus and pine forest", 2000, IMG_HILLS, "Sep – May", 10.2381, 77.4892, false);

        // ---- Beaches ----
        Destination gokarna = ensureDestination("Gokarna", "Karnataka", "beaches",
                "Quiet coves and cliffside trails away from the crowds", 1500, IMG_BEACH_1, "Oct – Mar", 14.5479, 74.3188, false);
        Destination andaman = ensureDestination("Andaman Islands", "Andaman & Nicobar", "beaches",
                "Turquoise water over coral, far from the mainland", 5200, IMG_BEACH_2, "Nov – May", 11.7401, 92.6586, false);
        Destination goa = ensureDestination("Goa", "Goa", "beaches",
                "India's beach capital, from Baga's parties to Palolem's calm", 2600, IMG_BEACH_1, "Nov – Feb", 15.5523, 73.7519, true);
        Destination varkala = ensureDestination("Varkala", "Kerala", "beaches",
                "Red cliffs dropping straight into the Arabian Sea", 2000, IMG_BEACH_2, "Nov – Mar", 8.7379, 76.7163, false);
        Destination pondicherry = ensureDestination("Pondicherry", "Puducherry", "beaches",
                "French Quarter streets that end at the promenade", 2100, IMG_BEACH_1, "Oct – Mar", 11.9416, 79.8083, false);
        Destination rameswaram = ensureDestination("Rameswaram", "Tamil Nadu", "beaches",
                "A sacred island town linked to the Ram Setu", 1700, IMG_BEACH_2, "Oct – Mar", 9.2876, 79.3129, false);

        // ---- Spiritual ----
        Destination varanasi = ensureDestination("Varanasi", "Uttar Pradesh", "spiritual",
                "Ghats, lamplit rituals and the pulse of the Ganges", 1600, IMG_SPIRITUAL, "Nov – Feb", 25.3176, 82.9739, true);
        Destination rishikesh = ensureDestination("Rishikesh", "Uttarakhand", "spiritual",
                "Yoga ashrams and river rafting on the Ganges", 1800, IMG_SPIRITUAL, "Sep – Apr", 30.0869, 78.2676, true);
        Destination amritsar = ensureDestination("Amritsar", "Punjab", "spiritual",
                "The Golden Temple and its endless free community kitchen", 1700, IMG_SPIRITUAL, "Oct – Mar", 31.6200, 74.8765, false);
        Destination bodhgaya = ensureDestination("Bodh Gaya", "Bihar", "spiritual",
                "Where the Buddha attained enlightenment under the Bodhi Tree", 1500, IMG_SPIRITUAL, "Oct – Mar", 24.6959, 84.9917, false);
        Destination tirupati = ensureDestination("Tirupati", "Andhra Pradesh", "spiritual",
                "One of the world's most-visited pilgrimage temples", 1600, IMG_SPIRITUAL, "Sep – Mar", 13.6288, 79.4192, false);
        Destination puri = ensureDestination("Puri", "Odisha", "spiritual",
                "The Jagannath Temple and a wide, working beach", 1700, IMG_SPIRITUAL, "Oct – Feb", 19.8135, 85.8312, false);
        Destination madurai = ensureDestination("Madurai", "Tamil Nadu", "spiritual",
                "The soaring, sculpted gopurams of Meenakshi Temple", 1600, IMG_SPIRITUAL, "Oct – Mar", 9.9195, 78.1193, false);
        Destination haridwar = ensureDestination("Haridwar", "Uttarakhand", "spiritual",
                "The Ganga Aarti at Har Ki Pauri, every evening without fail", 1500, IMG_SPIRITUAL, "Sep – Apr", 29.9457, 78.1642, false);

        // ---- Wildlife ----
        Destination ranthambore = ensureDestination("Ranthambore", "Rajasthan", "wildlife",
                "Tiger trails through ruined forts and dry deciduous forest", 3200, IMG_WILDLIFE, "Oct – Jun", 26.0173, 76.5026, false);
        Destination kaziranga = ensureDestination("Kaziranga National Park", "Assam", "wildlife",
                "The last stronghold of the one-horned rhinoceros", 3000, IMG_WILDLIFE, "Nov – Apr", 26.5775, 93.1714, false);
        Destination corbett = ensureDestination("Jim Corbett National Park", "Uttarakhand", "wildlife",
                "India's oldest national park, on the tiger trail since 1936", 3100, IMG_WILDLIFE, "Nov – Jun", 29.5300, 78.7747, false);
        Destination bandipur = ensureDestination("Bandipur National Park", "Karnataka", "wildlife",
                "Dry deciduous forest linking the Western and Eastern Ghats", 2700, IMG_WILDLIFE, "Oct – May", 11.6650, 76.6300, false);
        Destination gir = ensureDestination("Gir National Park", "Gujarat", "wildlife",
                "The only place on Earth to see wild Asiatic lions", 3300, IMG_WILDLIFE, "Dec – Apr", 21.1273, 70.7930, false);
        Destination sundarbans = ensureDestination("Sundarbans", "West Bengal", "wildlife",
                "The world's largest mangrove forest, home to the Bengal tiger", 3400, IMG_WILDLIFE, "Nov – Feb", 21.9497, 88.9468, false);

        // ---- Offbeat ----
        Destination spiti = ensureDestination("Spiti Valley", "Himachal Pradesh", "offbeat",
                "A cold desert of monasteries under impossible skies", 4200, IMG_OFFBEAT, "May – Sep", 32.2461, 78.0349, true);
        Destination ladakh = ensureDestination("Ladakh", "Ladakh", "offbeat",
                "High-altitude passes, monasteries and the Pangong shoreline", 4800, IMG_OFFBEAT, "May – Sep", 34.1526, 77.5771, true);
        Destination rannOfKutch = ensureDestination("Rann of Kutch", "Gujarat", "offbeat",
                "A white salt desert that turns silver under a full moon", 2800, IMG_OFFBEAT, "Nov – Feb", 23.7337, 69.8597, false);
        Destination majuli = ensureDestination("Majuli Island", "Assam", "offbeat",
                "The world's largest river island, on the Brahmaputra", 2200, IMG_OFFBEAT, "Oct – Mar", 26.9526, 94.1697, false);
        Destination tawang = ensureDestination("Tawang", "Arunachal Pradesh", "offbeat",
                "A remote monastery town near the Bhutan and Tibet borders", 3800, IMG_OFFBEAT, "Mar – Oct", 27.5859, 91.8594, false);
        Destination chopta = ensureDestination("Chopta", "Uttarakhand", "offbeat",
                "The 'Mini Switzerland of India', a trailhead into alpine meadows", 2400, IMG_OFFBEAT, "Apr – Jun", 30.4835, 79.1907, false);

        // ---- Hotels for a broad, representative subset ----
        ensureHotel(jaipur, "Haveli Amberwind", 4, 3400, IMG_HERITAGE_1, List.of("Free WiFi", "Rooftop pool", "Breakfast included"));
        ensureHotel(jaipur, "Zostel Jaipur", 3, 900, IMG_HERITAGE_2, List.of("Free WiFi", "Common lounge"));
        ensureHotel(munnar, "Tea County Resort", 4, 4200, IMG_HILLS, List.of("Mountain view", "Bonfire", "Free WiFi"));
        ensureHotel(varanasi, "Ganga View Homestay", 3, 1500, IMG_SPIRITUAL, List.of("River view", "Home-cooked meals"));
        ensureHotel(agra, "Taj Vista Heritage", 4, 3800, IMG_HERITAGE_1, List.of("Taj view rooms", "Free WiFi", "Breakfast included"));
        ensureHotel(agra, "Zostel Agra", 3, 1000, IMG_HERITAGE_2, List.of("Free WiFi", "Common lounge"));
        ensureHotel(goa, "Baga Beach Resort", 4, 4500, IMG_BEACH_1, List.of("Beachfront", "Pool", "Free WiFi"));
        ensureHotel(goa, "Palolem Beach Huts", 3, 1800, IMG_BEACH_2, List.of("Beachfront", "Hammocks"));
        ensureHotel(shimla, "Ridge View Inn", 4, 3200, IMG_HILLS, List.of("Mountain view", "Bonfire", "Free WiFi"));
        ensureHotel(manali, "Solang Pines Resort", 4, 3600, IMG_HILLS, List.of("Mountain view", "Bonfire", "Free WiFi"));
        ensureHotel(darjeeling, "Tea Garden Bungalow", 4, 3300, IMG_HILLS, List.of("Tea garden view", "Free WiFi"));
        ensureHotel(rishikesh, "Ganga Riverside Camp", 3, 1800, IMG_SPIRITUAL, List.of("River view", "Yoga sessions", "Bonfire"));
        ensureHotel(amritsar, "Golden Temple View Inn", 3, 1900, IMG_SPIRITUAL, List.of("Temple view", "Free WiFi", "Breakfast included"));
        ensureHotel(ladakh, "Pangong Camps", 3, 4200, IMG_OFFBEAT, List.of("Lake view", "Bonfire", "Home-cooked meals"));
        ensureHotel(coorg, "Coffee Estate Homestay", 4, 3100, IMG_HILLS, List.of("Estate walks", "Free WiFi", "Breakfast included"));
        ensureHotel(ooty, "Nilgiri Lake Resort", 4, 2900, IMG_HILLS, List.of("Lake view", "Free WiFi"));
        ensureHotel(kaziranga, "Wild Grass Lodge", 3, 3400, IMG_WILDLIFE, List.of("Safari desk", "Free WiFi", "Breakfast included"));
        ensureHotel(pondicherry, "French Quarter Boutique", 4, 3500, IMG_BEACH_1, List.of("Heritage building", "Free WiFi"));

        // ---- A few starter reviews ----
        ensureReview(jaipur, "Ananya R.", 5, true, "2026-06-12", "Amber Fort at sunrise, before the crowds, was worth the early alarm.");
        ensureReview(jaipur, "Kabir S.", 4, true, "2026-05-02", "Old city traffic is intense, but the bazaars make up for it.");
        ensureReview(munnar, "Meera P.", 5, true, "2026-04-18", "Woke up to clouds sitting inside the valley. Unreal.");
        ensureReview(agra, "Rohit T.", 5, true, "2026-03-10", "Go at sunrise. The marble genuinely changes colour through the morning.");
        ensureReview(goa, "Priya S.", 4, true, "2026-01-22", "Baga is loud, Palolem is calm — pick based on what kind of trip you want.");
        ensureReview(ladakh, "Arjun K.", 5, true, "2026-07-05", "Pangong Lake at sunset is the single best view I've seen in India.");
    }

    private Destination ensureDestination(String name, String state, String category, String tagline, int priceFrom,
                                           String image, String bestSeason, double lat, double lng, boolean featured) {
        return destinationRepository.findByName(name).orElseGet(() -> {
            Destination d = new Destination();
            d.setName(name);
            d.setState(state);
            d.setCategory(category);
            d.setTagline(tagline);
            d.setDescription(tagline);
            d.setRating(4.6);
            d.setReviewCount(0);
            d.setPriceFrom(priceFrom);
            d.setImage(image);
            d.setBestSeason(bestSeason);
            d.setLat(lat);
            d.setLng(lng);
            d.setFeatured(featured);
            return destinationRepository.save(d);
        });
    }

    private void ensureHotel(Destination destination, String name, int stars, int pricePerNight, String image, List<String> amenities) {
        if (hotelRepository.existsByDestinationIdAndName(destination.getId(), name)) return;
        Hotel h = new Hotel();
        h.setDestination(destination);
        h.setName(name);
        h.setStars(stars);
        h.setPricePerNight(pricePerNight);
        h.setRating(4.5);
        h.setReviewCount(0);
        h.setImage(image);
        h.setAmenities(amenities);
        hotelRepository.save(h);
    }

    private void ensureReview(Destination destination, String author, int rating, boolean verified, String date, String text) {
        boolean alreadyExists = reviewRepository.findByDestinationIdOrderByDateDesc(destination.getId()).stream()
                .anyMatch(r -> r.getAuthor().equals(author) && r.getText().equals(text));
        if (alreadyExists) return;

        Review r = new Review();
        r.setDestination(destination);
        r.setAuthor(author);
        r.setRating(rating);
        r.setVerified(verified);
        r.setDate(LocalDate.parse(date));
        r.setText(text);
        reviewRepository.save(r);
    }
}
