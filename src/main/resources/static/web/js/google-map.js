
let map, marker;
let isValidAddress = false;

function initAutocomplete() {
    const input = document.getElementById('address');
    console.log("Khởi tạo Autocomplete với input:", input);

    const autocomplete = new google.maps.places.Autocomplete(input, {
        componentRestrictions: { country: 'VN' },
    });

    autocomplete.setFields(['address_component', 'geometry', 'formatted_address']);
    console.log("Các trường được thiết lập cho Autocomplete: ['address_component', 'geometry', 'formatted_address']");

    autocomplete.addListener('place_changed', function() {
        console.log("Sự kiện place_changed đã được kích hoạt");
        const place = autocomplete.getPlace();
        console.log("Địa chỉ đã chọn: ", place);

        if (!place.geometry) {
            console.error('Không tìm thấy vị trí trong dữ liệu Autocomplete');
            return;
        }

        const lat = place.geometry.location.lat();
        const lng = place.geometry.location.lng();
        console.log("Tọa độ địa chỉ đã chọn: Vĩ độ:", lat, "Kinh độ:", lng);

        if (place.formatted_address && place.formatted_address.trim() !== '') {
            document.getElementById('address').value = place.formatted_address;
            isValidAddress = true;
            console.log("Địa chỉ hợp lệ từ Autocomplete:", place.formatted_address);
        } else {
            isValidAddress = false;
            console.log("Địa chỉ không hợp lệ từ Autocomplete (formatted_address bị thiếu hoặc rỗng).");
        }

        if (marker) {
            marker.setPosition(place.geometry.location);
        } else {
            placeMarker(place.geometry.location);
        }

        map.setCenter(place.geometry.location);
        map.setZoom(15);
        console.log("Đã đặt lại vị trí trung tâm của bản đồ vào:", place.geometry.location);
    });

    input.addEventListener('input', function() {
        console.log("Ô nhập liệu đã thay đổi:", input.value);
        isValidAddress = false;
        if (input.value.trim() === '') {
            console.log("Ô nhập trống. isValidAddress được đặt thành false");
        }
    });
}


// Hàm khởi tạo bản đồ
function initMap() {
    console.log("Khởi tạo bản đồ bắt đầu");

    map = new google.maps.Map(document.getElementById('map'), {
        center: {lat: 10.8231, lng: 106.6297},
        zoom: 12
    });
    console.log("Bản đồ đã được khởi tạo với vị trí trung tâm tại TPHCM");

    map.addListener('click', function(event) {
        console.log("Sự kiện click trên bản đồ đã được kích hoạt");
        placeMarker(event.latLng);
        isValidAddress = true;
        console.log("Địa chỉ đã được chọn từ bản đồ. isValidAddress được đặt thành true");
    });
}

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

    const geocoder = new google.maps.Geocoder();
    geocoder.geocode({location: location}, function(results, status) {
        console.log("Kết quả Geocoding:", results, "Trạng thái:", status);

        if (status === 'OK' && results[0]) {
            const address = results[0].formatted_address;
            document.getElementById('address').value = address;
            isValidAddress = true;
            console.log("Địa chỉ từ Geocoding:", address);
        } else {
            console.error("Lỗi Geocoding hoặc không tìm thấy kết quả");
        }
    });
}

google.maps.event.addDomListener(window, 'load', function() {
    console.log("Google Maps đã được tải. Đang khởi tạo bản đồ và Autocomplete.");
    initMap();
    initAutocomplete();
});