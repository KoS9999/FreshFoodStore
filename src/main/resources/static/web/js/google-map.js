
let map, marker;
let isValidAddress = false; // Biến kiểm tra nếu địa chỉ hợp lệ

function initAutocomplete() {
    const input = document.getElementById('address');
    console.log("Khởi tạo Autocomplete với input:", input);

    // Khởi tạo Autocomplete với giới hạn quốc gia là Việt Nam
    const autocomplete = new google.maps.places.Autocomplete(input, {
        componentRestrictions: { country: 'VN' },  // Giới hạn gợi ý chỉ trong Việt Nam
    });

    autocomplete.setFields(['address_component', 'geometry', 'formatted_address']);
    console.log("Các trường được thiết lập cho Autocomplete: ['address_component', 'geometry', 'formatted_address']");

    autocomplete.addListener('place_changed', function() {
        console.log("Sự kiện place_changed đã được kích hoạt");
        const place = autocomplete.getPlace();
        console.log("Địa chỉ đã chọn: ", place);

        // Kiểm tra nếu không có geometry, thì không tiếp tục
        if (!place.geometry) {
            console.error('Không tìm thấy vị trí trong dữ liệu Autocomplete');
            return;
        }

        const lat = place.geometry.location.lat();
        const lng = place.geometry.location.lng();
        console.log("Tọa độ địa chỉ đã chọn: Vĩ độ:", lat, "Kinh độ:", lng);

        // Kiểm tra nếu 'formatted_address' có giá trị hợp lệ
        if (place.formatted_address && place.formatted_address.trim() !== '') {
            document.getElementById('address').value = place.formatted_address;
            isValidAddress = true; // Đặt cờ là hợp lệ khi chọn địa chỉ từ gợi ý
            console.log("Địa chỉ hợp lệ từ Autocomplete:", place.formatted_address);
        } else {
            isValidAddress = false; // Nếu không có địa chỉ hợp lệ, đặt lại isValidAddress thành false
            console.log("Địa chỉ không hợp lệ từ Autocomplete (formatted_address bị thiếu hoặc rỗng).");
        }

        // Di chuyển marker trên bản đồ và zoom vào vị trí đó
        if (marker) {
            marker.setPosition(place.geometry.location);
        } else {
            placeMarker(place.geometry.location);
        }

        map.setCenter(place.geometry.location);
        map.setZoom(15);
        console.log("Đã đặt lại vị trí trung tâm của bản đồ vào:", place.geometry.location);
    });

    // Lắng nghe sự kiện khi người dùng thay đổi hoặc xóa nội dung trong ô nhập địa chỉ
    input.addEventListener('input', function() {
        console.log("Ô nhập liệu đã thay đổi:", input.value);
        isValidAddress = false;
        if (input.value.trim() === '') {
            // Nếu ô nhập trống, đặt lại isValidAddress thành false
            console.log("Ô nhập trống. isValidAddress được đặt thành false");
        }
    });
}


// Hàm khởi tạo bản đồ
function initMap() {
    console.log("Khởi tạo bản đồ bắt đầu");

    // Khởi tạo bản đồ tại vị trí mặc định (TPHCM)
    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 10.8231, lng: 106.6297}, // Vị trí mặc định TPHCM
        zoom: 12
    });
    console.log("Bản đồ đã được khởi tạo với vị trí trung tâm tại TPHCM");

    // Lắng nghe sự kiện khi người dùng click vào bản đồ
    map.addListener('click', function(event) {
        console.log("Sự kiện click trên bản đồ đã được kích hoạt");
        placeMarker(event.latLng);
        isValidAddress = true; // Khi người dùng chọn vị trí trên bản đồ, coi là hợp lệ
        console.log("Địa chỉ đã được chọn từ bản đồ. isValidAddress được đặt thành true");
    });
}

// Hàm để thêm marker vào bản đồ
function placeMarker(location) {
    console.log("Đặt marker tại vị trí:", location);

    if (marker) {
        marker.setPosition(location);
    } else {
        marker = new google.maps.Marker({
            position: location,
            map: map
        });
    }

    // Sử dụng Geocoding API để lấy địa chỉ từ tọa độ
    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({location: location}, function(results, status) {
        console.log("Kết quả Geocoding:", results, "Trạng thái:", status);

        if (status === 'OK' && results[0]) {
            const address = results[0].formatted_address;
            document.getElementById('address').value = address;
            isValidAddress = true; // Đặt cờ là hợp lệ khi chọn địa chỉ trên bản đồ
            console.log("Địa chỉ từ Geocoding:", address);
        } else {
            console.error("Lỗi Geocoding hoặc không tìm thấy kết quả");
        }
    });
}

// Gọi hàm khởi tạo khi trang đã load
google.maps.event.addDomListener(window, 'load', function() {
    console.log("Google Maps đã được tải. Đang khởi tạo bản đồ và Autocomplete.");
    initMap();
    initAutocomplete();
});