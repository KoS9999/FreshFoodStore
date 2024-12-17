document.addEventListener('DOMContentLoaded', function () {
    console.log('DOM Loaded');

    // Xóa sản phẩm khỏi Wishlist
    const removeButtons = document.querySelectorAll('.remove-wishlist');
    removeButtons.forEach(button => {
        button.addEventListener('click', function (e) {
            e.preventDefault();
            const productId = this.getAttribute('data-product-id');
            console.log('Remove product from wishlist:', productId);

            fetch(`/wishlist/remove?productId=${productId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            })
                .then(response => {
                    if (response.ok) {
                        console.log('Product removed from wishlist:', productId);
                        // Reload lại trang để cập nhật danh sách
                        location.reload();
                    } else {
                        throw new Error('Failed to remove product from wishlist');
                    }
                })
                .catch(error => {
                    console.error('Error:', error);
                    alert('Could not remove product from wishlist');
                });
        });
    });
});



