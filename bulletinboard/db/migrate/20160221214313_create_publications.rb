class CreatePublications < ActiveRecord::Migration
  def change
      create_table :publications, id: false do |t|
          t.binary :content
      end
  end
end
